package necromunda;

import java.util.HashMap;
import java.util.Map;

import com.jme3.asset.AssetManager;
import com.jme3.asset.TextureKey;
import com.jme3.material.Material;
import com.jme3.material.RenderState.BlendMode;
import com.jme3.material.plugins.NeoTextureMaterialKey;
import com.jme3.math.ColorRGBA;
import com.jme3.texture.Texture;

public class MaterialFactory {
	public enum MaterialIdentifier {
		NORMAL,
		SELECTED,
		TARGETED,
		PATH,
		SYMBOL_PINNED,
		SYMBOL_DOWN,
		SYMBOL_SEDATED,
		SYMBOL_COMATOSE,
		SYMBOL_LADDER,
		TABLE_GRASS;
	}
	
	AssetManager assetManager;
	Map<Enum<MaterialIdentifier>, Material> materialMap = new HashMap<Enum<MaterialIdentifier>, Material>();
	
	public MaterialFactory(AssetManager assetManager) {
		this.assetManager = assetManager;
		
		Material baseMaterial = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
		baseMaterial.setFloat("Shininess", 5f);
		baseMaterial.setColor("Diffuse", new ColorRGBA(64 / 255f, 147 / 255f, 91 / 255f, 1.0f));
		baseMaterial.setBoolean("UseMaterialColors", true);
		
		baseMaterial = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
		Texture modelTexture = assetManager.loadTexture("Textures/Grass01.gif");
		baseMaterial.setTexture("DiffuseMap", modelTexture);
		baseMaterial.setFloat("Shininess", 100f);
		
		Material selectedBaseMaterial = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
		selectedBaseMaterial.setFloat("Shininess", 5f);
		selectedBaseMaterial.setColor("Diffuse", new ColorRGBA(ColorRGBA.Red));
		selectedBaseMaterial.setBoolean("UseMaterialColors", true);

		Material targetedMaterial = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
		targetedMaterial.setFloat("Shininess", 5f);
		targetedMaterial.setColor("Diffuse", new ColorRGBA(ColorRGBA.Yellow));
		targetedMaterial.setBoolean("UseMaterialColors", true);

		Material pathMaterial = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
		pathMaterial.setFloat("Shininess", 5f);
		pathMaterial.setColor("Diffuse", new ColorRGBA(0.5f, 0.5f, 0.5f, 0.5f));
		pathMaterial.setBoolean("UseMaterialColors", true);
		pathMaterial.getAdditionalRenderState().setBlendMode(BlendMode.Alpha);

		Material pinnedSymbolMaterial = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
		Texture pinnedSymbolTexture = assetManager.loadTexture("Textures/Pinned.PNG");
		pinnedSymbolMaterial.setTexture("ColorMap", pinnedSymbolTexture);
		
		Material downSymbolMaterial = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
		Texture downSymbolTexture = assetManager.loadTexture("Textures/Down.PNG");
		downSymbolMaterial.setTexture("ColorMap", downSymbolTexture);
		
		Material sedatedSymbolMaterial = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
		Texture sedatedSymbolTexture = assetManager.loadTexture("Textures/Sedated.PNG");
		sedatedSymbolMaterial.setTexture("ColorMap", sedatedSymbolTexture);
		
		Material comatoseSymbolMaterial = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
		Texture comatoseSymbolTexture = assetManager.loadTexture("Textures/Comatose.PNG");
		comatoseSymbolMaterial.setTexture("ColorMap", comatoseSymbolTexture);
		
		Material ladderSymbolMaterial = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
		Texture ladderSymbolTexture = assetManager.loadTexture("Textures/Ladder.PNG");
		ladderSymbolMaterial.setTexture("ColorMap", ladderSymbolTexture);
		
		NeoTextureMaterialKey key = new NeoTextureMaterialKey("Textures/grass01.tgr");
		Material tableMaterial = assetManager.loadAsset(key);
		tableMaterial.setFloat("Shininess", 12f);
		
		materialMap.put(MaterialIdentifier.NORMAL, baseMaterial);
		materialMap.put(MaterialIdentifier.SELECTED, selectedBaseMaterial);
		materialMap.put(MaterialIdentifier.TARGETED, targetedMaterial);
		materialMap.put(MaterialIdentifier.PATH, pathMaterial);
		materialMap.put(MaterialIdentifier.SYMBOL_PINNED, pinnedSymbolMaterial);
		materialMap.put(MaterialIdentifier.SYMBOL_DOWN, downSymbolMaterial);
		materialMap.put(MaterialIdentifier.SYMBOL_SEDATED, sedatedSymbolMaterial);
		materialMap.put(MaterialIdentifier.SYMBOL_COMATOSE, comatoseSymbolMaterial);
		materialMap.put(MaterialIdentifier.SYMBOL_LADDER, ladderSymbolMaterial);
		materialMap.put(MaterialIdentifier.TABLE_GRASS, tableMaterial);
	}
	
	public Material createMaterial(Enum<MaterialIdentifier> identifier) {
		return materialMap.get(identifier);
	}
	
	public Material createBuildingMaterial(String identifier) {
		Material buildingMaterial = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
		buildingMaterial.setFloat("Shininess", 5f);
		TextureKey key = new TextureKey("Textures/NecromundaBuilding" + identifier + ".png", false);
		Texture modelTexture = assetManager.loadTexture(key);
		buildingMaterial.setTexture("DiffuseMap", modelTexture);
		buildingMaterial.getAdditionalRenderState().setBlendMode(BlendMode.Alpha);
		
		return buildingMaterial;
	}
	
	public Material createFigureMaterial(Fighter fighter) {
		BasedModelImage basedModelImage = fighter.getGangerPicture();
		Material figureMaterial = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
		Texture modelTexture = assetManager.loadTexture(basedModelImage.getRelativeImageFileName());
		figureMaterial.setTexture("ColorMap", modelTexture);
		figureMaterial.getAdditionalRenderState().setBlendMode(BlendMode.Alpha);
		
		return figureMaterial;
	}
}
