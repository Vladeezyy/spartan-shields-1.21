package com.oblivioussp.spartanshields.client.render.item;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.resources.model.Material;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public class TowerShieldRenderInfo
{
	protected final ResourceLocation textureNoPattern;
	protected final ResourceLocation texturePattern;
	
	protected final Material materialNoPattern;
	protected final Material materialPattern;
	
	public TowerShieldRenderInfo(ResourceLocation texNoPattern, ResourceLocation texPattern)
	{
		textureNoPattern = texNoPattern;
		texturePattern = texPattern;
		
		materialNoPattern = new Material(Sheets.SHIELD_SHEET, textureNoPattern);
		materialPattern = new Material(Sheets.SHIELD_SHEET, texturePattern);
	}
	
	public Material getMaterial(boolean isPatterned)
	{
		return isPatterned ? materialPattern : materialNoPattern;
	}
	
	public boolean hasLayers()
	{
		return false;
	}
	
	public RenderType getLayerRenderType(ItemStack stack)
	{
		return RenderType.solid();
	}
	
	public float getColourRed()
	{
		return 1.0f;
	}
	
	public float getColourGreen()
	{
		return 1.0f;
	}
	
	public float getColourBlue()
	{
		return 1.0f;
	}
}