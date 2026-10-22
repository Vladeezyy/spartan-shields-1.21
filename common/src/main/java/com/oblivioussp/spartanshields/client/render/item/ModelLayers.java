package com.oblivioussp.spartanshields.client.render.item;

import com.oblivioussp.spartanshields.ModSpartanShields;

import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.Identifier;

public class ModelLayers 
{
	public static final ModelLayerLocation BASE_SHIELD = createMainModelLayer("base_shield");
	public static final ModelLayerLocation KITE_SHIELD = createMainModelLayer("kite_shield");
	public static final ModelLayerLocation TOWER_SHIELD = createMainModelLayer("tower_shield");
	public static final ModelLayerLocation LUMIUM_SHIELD = createMainModelLayer("lumium_shield");
	public static final ModelLayerLocation ENDERIUM_SHIELD = createMainModelLayer("enderium_shield");
	public static final ModelLayerLocation MANASTEEL_SHIELD = createMainModelLayer("manasteel_shield");
	public static final ModelLayerLocation TERRASTEEL_SHIELD = createMainModelLayer("terrasteel_shield");
	public static final ModelLayerLocation ELEMENTIUM_SHIELD = createMainModelLayer("elementium_shield");
	public static final ModelLayerLocation MEKANISM_SHIELD = createMainModelLayer("mekanism_shield");
	public static final ModelLayerLocation DARK_STEEL_SHIELD = createMainModelLayer("dark_steel_shield");
	
	public static ModelLayerLocation createMainModelLayer(String location)
	{
		return new ModelLayerLocation(Identifier.fromNamespaceAndPath(ModSpartanShields.ID, location), "main");
	}
}
