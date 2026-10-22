package com.oblivioussp.spartanshields.client.render.item;

import com.oblivioussp.spartanshields.ModSpartanShields;

import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;

public class DarkSteelTowerShieldRenderInfo extends TowerShieldRenderInfo 
{
	protected final float r, g, b;

	// 1.21.9+: eyes is translucent (the black of the "lights on" texture would be drawn); energy_swirl adds the colour
	// as eyes did before (no texture scrolling with the offset 0, 0)
	protected static final RenderType LIGHTS_ON = RenderTypes.energySwirl(Identifier.fromNamespaceAndPath(ModSpartanShields.ID, "textures/entity/enderio/dark_steel_tower_shield_lights_on.png"), 0.0F, 0.0F);
	protected static final RenderType LIGHTS_OFF = RenderTypes.entityTranslucent(Identifier.fromNamespaceAndPath(ModSpartanShields.ID, "textures/entity/enderio/dark_steel_tower_shield_lights_off.png"));
	

	public DarkSteelTowerShieldRenderInfo(float rIn, float gIn, float bIn) 
	{
		super(Identifier.fromNamespaceAndPath(ModSpartanShields.ID, "entity/enderio/dark_steel_tower_shield_nopattern"),
				Identifier.fromNamespaceAndPath(ModSpartanShields.ID, "entity/enderio/dark_steel_tower_shield_pattern"));
		r = rIn;
		g = gIn;
		b = bIn;
	}
	
	@Override
	public boolean hasLayers() 
	{
		return true;
	}
	
	@Override
	public RenderType getLayerRenderType(boolean powered) 
	{
		return powered ? LIGHTS_ON : LIGHTS_OFF;
	}
	
	@Override
	public float getColourRed() 
	{
		return r;
	}
	
	@Override
	public float getColourGreen() 
	{
		return g;
	}

	@Override
	public float getColourBlue() 
	{
		return b;
	}
}
