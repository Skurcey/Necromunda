package necromunda;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import weapons.RangeCombatWeapon;
import weapons.Weapon;
import weapons.WebPistol;

public abstract class Fighter implements Serializable {
	public static FighterProfile getTemplateProfile() {
		return new GangerProfile();
	}

	public enum State {
		NORMAL("Normal"),
		PINNED("Pinned"),
		DOWN("Down"),
		SEDATED("Sedated"),
		COMATOSE("Comatose"),
		BROKEN("Broken"),
		OUT_OF_ACTION("Out of Action");
		
		private String literal;
		
		private State(String literal) {
			this.literal = literal;
		}
		
		@Override
		public String toString() {
			return literal;
		}
	}
	
	public enum Type {
		LEADER("Leader", Leader.class),
		GANGER("Ganger", Ganger.class),
		JUVE("Juve", Juve.class),
		HEAVY("Heavy", Heavy.class);
		
		private String literal;
		private Class<? extends Fighter> associatedClass;
		
		private Type(String literal, Class<? extends Fighter> associatedClass) {
			this.literal = literal;
			this.associatedClass = associatedClass;
		}
		
		@Override
		public String toString() {
			return literal;
		}

		public Class<? extends Fighter> getAssociatedClass() {
			return associatedClass;
		}
	}
	
	private FighterProfile profile;
	private State state;
	private boolean hasMoved;
	private boolean canMove;
	private boolean hasRun;
	private boolean canRun;
	private boolean hasShot;
	private boolean canShoot;
	private boolean isWebbed;
	private boolean isHidden;
	private float baseRadius;
	private float remainingMovementDistance;
	private int remainingWounds;
	private int fleshWounds;
	private boolean isGoingToRun;
	private boolean isSpotted;
	private Gang gang;
	private RangeCombatWeapon selectedRangeCombatWeapon;
	private List<Weapon> weapons;
	private int cost;
	private BasedModelImage gangerPicture;
	private String name;
	
	public static Fighter getInstance(Type type, String name, Gang ownGang) {
		Fighter fighter = null;
		
		switch (type) {
			case LEADER:
				fighter = new Leader(name, new LeaderProfile(), ownGang);
				break;
			case GANGER:
				fighter = new Ganger(name, new GangerProfile(), ownGang);
				break;
			case JUVE:
				fighter = new Juve(name, new JuveProfile(), ownGang);
				break;
			case HEAVY:
				fighter = new Heavy(name, new HeavyProfile(), ownGang);
		}
		
		return fighter;
	}
	
	public Fighter(String name, FighterProfile profile, Gang ownGang) {
		this.name = name;
		this.profile = profile;
		this.gang = ownGang;
		
		state = State.NORMAL;
		canMove = true;
		canRun = true;
		canShoot = true;
		isHidden = false;
		baseRadius = 0.5f;
		remainingMovementDistance = profile.getMovement();
		remainingWounds = profile.getWounds();
		weapons = new ArrayList<Weapon>();
	}
	
	/*private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
		in.defaultReadObject();
		List<Weapon> tempWeapons = new ArrayList<Weapon>(weapons);
		weapons.clear();
		
		for (Weapon weapon : tempWeapons) {
			addWeapon(weapon);
		}
	}*/
	
	public void addWeapon(Weapon weapon) {
		weapons.add(weapon);
	}
	
	public void removeAllWeapons() {
		weapons.clear();
	}
	
	public void injure(boolean highImpact) {
		int injuryRoll = Utils.rollD6();
		State tempState = null;
		
		if (injuryRoll == 1) {
			fleshWounds++;
			tempState = state;
			
			if ((fleshWounds >= profile.getWeaponSkill()) && (fleshWounds >= profile.getBallisticSkill())) {
				tempState = State.OUT_OF_ACTION;
			}
		}
		else if ((injuryRoll > 1) && (injuryRoll < 5)) {
			tempState = State.DOWN;
		}
		else if (injuryRoll == 5) {
			if (highImpact) {
				tempState = State.OUT_OF_ACTION;
			}
			else {
				tempState = State.DOWN;
			}
		}
		else {
			tempState = State.OUT_OF_ACTION;
		}
		
		if (tempState.equals(State.DOWN)) {
			if (state.equals(State.NORMAL) || state.equals(State.PINNED)) {
				state = tempState;
			}
		}
		else if (tempState.equals(State.OUT_OF_ACTION)) {
			state = tempState;
		}
	}
	
	public void setNextNormalState() {
		int injuryRoll = Utils.rollD6();
		
		if (injuryRoll == 1) {
			fleshWounds++;
			
			if ((fleshWounds >= profile.getWeaponSkill()) && (fleshWounds >= profile.getBallisticSkill())) {
				state = State.OUT_OF_ACTION;
			}
			else {
				state = State.PINNED;
			}
		}
		else if ((injuryRoll > 1) && (injuryRoll < 6)) {
			state = State.DOWN;
		}
		else {
			state = State.OUT_OF_ACTION;
		}
	}
	
	public void poison() {
		int injuryRoll = Utils.rollD6();
		State tempState = state;
		
		if ((injuryRoll == 1) || (injuryRoll == 2)) {
		}
		else if ((injuryRoll == 3) || (injuryRoll == 4)) {
			tempState = State.SEDATED;
		}
		else if (injuryRoll == 5) {
			tempState = State.COMATOSE;
		}
		else {
			tempState = State.OUT_OF_ACTION;
		}
		
		if (tempState.equals(State.PINNED)) {
			if (state.equals(State.NORMAL)) {
				state = tempState;
			}
		}
		else if (tempState.equals(State.SEDATED) || tempState.equals(State.COMATOSE)) {
			if (state.equals(State.NORMAL) || state.equals(State.PINNED)) {
				state = tempState;
			}
		}
		else if (tempState.equals(State.OUT_OF_ACTION)) {
			state = tempState;
		}
	}
	
	public void setNextPoisonedState() {
		int injuryRoll = Utils.rollD6();
		
		if ((injuryRoll == 1) || (injuryRoll == 2)) {
			setState(State.PINNED);
		}
		else if ((injuryRoll == 3) || (injuryRoll == 4)) {
			setState(State.SEDATED);
		}
		else if (injuryRoll == 5) {
			setState(State.COMATOSE);
		}
		else {
			setState(State.OUT_OF_ACTION);
		}
	}
	
	public void breakWeb() {
		if (isWebbed()) {
			int webRoll = Utils.rollD6();

			if ((webRoll + getStrength()) >= 9) {
				setWebbed(false);
				Necromunda.appendToStatusMessage("This ganger has broken the web.");
			}
			else {
				WebPistol.dealWebDamageTo(this);
			}
		}
		else {
			Necromunda.setStatusMessage("This ganger is not webbed.");
		}
	}
	
	public void resetRemainingMovementDistance() {
		if (state.equals(State.DOWN) || state.equals(State.SEDATED)) {
			setRemainingMovementDistance(2);
		}
		else if (state.equals(State.COMATOSE)) {
			setRemainingMovementDistance(0);
		}
		else {
			setRemainingMovementDistance(profile.getMovement());
		}
	}
	
	public void setIsGoingToRun(boolean isGoingToRun) {
		this.isGoingToRun = isGoingToRun;
		resetRemainingMovementDistance();
		
		if (isGoingToRun) {
			remainingMovementDistance *= 2;
		}
	}
	
	public boolean isGoingToRun() {
		return isGoingToRun;
	}
	
	public void turnStarted() {
		setIsGoingToRun(false);
		setHasMoved(false);
		setHasRun(false);
		setHasShot(false);
		resetRemainingMovementDistance();
		
		for (Weapon weapon : weapons) {
			weapon.turnStarted();
		}
	}
	
	public void turnEnded() {
		if (state.equals(State.PINNED)) {
			state = State.NORMAL;
		}
		else if (state.equals(State.DOWN)) {
			setNextNormalState();
		}
		else if (state.equals(State.SEDATED) || state.equals(State.COMATOSE)) {
			setNextPoisonedState();
		}
	}
	
	public void unpinByInitiative() {
		int initiativeRoll = Utils.rollD6();
		
		if (initiativeRoll <= profile.getInitiative()) {
			state = State.NORMAL;
			Necromunda.appendToStatusMessage(String.format("%s unpins by initiative.", this));
		}
		else {
			Necromunda.appendToStatusMessage(String.format("%s fails to unpin by initiative.", this));
		}
	}
	
	public int getGrenadeRange() {
		int range = profile.getStrength() * 2 + 2;
		
		if (range > 12) {
			range = 12;
		}
		
		return range;
	}
	
	public List<Weapon> getWeapons() {
		return weapons;
	}

	public float getBaseRadius() {
		return baseRadius;
	}

	public void setBaseRadius(float baseRadius) {
		this.baseRadius = baseRadius;
	}

	public State getState() {
		return state;
	}

	public void setState(State state) {
		this.state = state;
	}

	public float getRemainingMovementDistance() {
		return remainingMovementDistance;
	}

	public void setRemainingMovementDistance(float remainingMovementDistance) {
		this.remainingMovementDistance = remainingMovementDistance;
	}

	public Gang getGang() {
		return gang;
	}

	public void setGang(Gang gang) {
		this.gang = gang;
	}

	public boolean isSpotted() {
		return isSpotted;
	}

	public void setSpotted(boolean isSpotted) {
		this.isSpotted = isSpotted;
	}

	public RangeCombatWeapon getSelectedRangeCombatWeapon() {
		return selectedRangeCombatWeapon;
	}

	public void setSelectedRangeCombatWeapon(
			RangeCombatWeapon selectedRangeCombatWeapon) {
		this.selectedRangeCombatWeapon = selectedRangeCombatWeapon;
	}

	public FighterProfile getProfile() {
		return profile;
	}

	public int getRemainingWounds() {
		return remainingWounds;
	}

	public void setRemainingWounds(int remainingWounds) {
		this.remainingWounds = remainingWounds;
	}

	public boolean hasMoved() {
		return hasMoved;
	}

	public void setHasMoved(boolean hasMoved) {
		this.hasMoved = hasMoved;
	}

	public int getFleshWounds() {
		return fleshWounds;
	}

	public void setFleshWounds(int fleshWounds) {
		this.fleshWounds = fleshWounds;
	}

	public int getCost() {
		return cost;
	}

	public void setCost(int cost) {
		this.cost = cost;
	}

	public int getValue() {
		int value = getCost();
		
		for (Weapon weapon : weapons) {
			value += weapon.getCost();
		}
		
		return value;
	}

	public BasedModelImage getGangerPicture() {
		return gangerPicture;
	}

	public void setGangerPicture(BasedModelImage gangerPicture) {
		this.gangerPicture = gangerPicture;
	}
	
	public boolean isReliableMate() {
		if (state.equals(State.NORMAL) || state.equals(State.PINNED)) {
			return true;
		}
		else {
			return false;
		}
	}

	public boolean canShoot() {
		boolean canShoot = true;
		
		if (!this.canShoot || hasShot || hasRun || !state.equals(State.NORMAL)) {
			canShoot = false;
		}
		
		return canShoot;
	}

	public void setCanShoot(boolean canShoot) {
		this.canShoot = canShoot;
	}
	
	public boolean canMove() {
		boolean canMove = true;
		
		if (!this.canMove || (remainingMovementDistance == 0) || state.equals(State.COMATOSE) || isWebbed || state.equals(State.PINNED)) {
			canMove = false;
		}
		
		return canMove;
	}

	public void setCanMove(boolean canMove) {
		this.canMove = canMove;
	}	
	
	public boolean hasRun() {
		return hasRun;
	}

	public void setHasRun(boolean hasRun) {
		this.hasRun = hasRun;
	}

	public boolean canRun() {
		boolean canRun = true;
		
		/*List<? extends Fighter> hostileGangers = gang.getHostileGangers(game.getGangs());
		List<Fighter> visibleHostileGangers = game.getVisibleObjects(position, hostileGangers);

		for (Fighter object : visibleHostileGangers) {
			float runSpotDistanceBetweenPositions = Necromunda.RUN_SPOT_DISTANCE + this.getRadius() + object.getRadius();
			
			if (position.distance(object.getPosition()) <= runSpotDistanceBetweenPositions) {
				return false;
			}
		}*/
		
		if (!this.canMove || !this.canRun || !state.equals(State.NORMAL)) {
			return false;
		}

		return canRun;
	}

	public void setCanRun(boolean canRun) {
		this.canRun = canRun;
	}

	public boolean hasShot() {
		return hasShot;
	}

	public void setHasShot(boolean hasShot) {
		this.hasShot = hasShot;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return name;
	}

	public boolean isWebbed() {
		return isWebbed;
	}

	public void setWebbed(boolean isWebbed) {
		this.isWebbed = isWebbed;
	}
	
	public boolean isHidden() {
		return isHidden;
	}

	public void setHidden(boolean isHidden) {
		this.isHidden = isHidden;
	}

	public int getMovement() {
		return profile.getMovement();
	}
	
	public int getWeaponSkill() {
		int weaponSkill = profile.getWeaponSkill() - fleshWounds;
		return weaponSkill;
	}
	
	public int getBallisticSkill() {
		int ballisticSkill = profile.getBallisticSkill() - fleshWounds;
		return ballisticSkill;
	}
	
	public int getStrength() {
		return profile.getStrength();
	}
	
	public int getToughness() {
		return profile.getToughness();
	}
	
	public int getWounds() {
		return profile.getWounds();
	}
	
	public int getInitiative() {
		return profile.getInitiative();
	}
	
	public int getAttacks() {
		return profile.getAttacks();
	}
	
	public int getLeadership() {
		return profile.getLeadership();
	}
	
	public boolean isBroken() {
		return state.equals(State.BROKEN);
	}
	
	public boolean isComatose() {
		return state.equals(State.COMATOSE);
	}
	
	public boolean isDown() {
		return state.equals(State.DOWN);
	}
	
	public boolean isNormal() {
		return state.equals(State.NORMAL);
	}
	
	public boolean isOutOfAction() {
		return state.equals(State.OUT_OF_ACTION);
	}
	
	public boolean isPinned() {
		return state.equals(State.PINNED);
	}
	
	public boolean isSedated() {
		return state.equals(State.SEDATED);
	}
}
