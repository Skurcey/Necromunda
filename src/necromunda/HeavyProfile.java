package necromunda;

public class HeavyProfile extends FighterProfile {
	/**
	 * 
	 */
	private static final long serialVersionUID = 903989683024366321L;

	public HeavyProfile() {
		setMovement(4);
		setWeaponSkill(3);
		setBallisticSkill(3);
		setStrength(3);
		setToughness(3);
		setWounds(1);
		setInitiative(3);
		setAttacks(1);
		setLeadership(7);
	}
}
