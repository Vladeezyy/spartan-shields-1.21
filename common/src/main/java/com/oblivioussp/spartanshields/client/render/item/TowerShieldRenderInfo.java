package com.oblivioussp.spartanshields.client.render.item;

import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.resources.model.Material;
import net.minecraft.resources.Identifier;

public class TowerShieldRenderInfo
{
	protected final Identifier textureNoPattern;
	protected final Identifier texturePattern;
	
	protected final Material materialNoPattern;
	protected final Material materialPattern;
	
	public TowerShieldRenderInfo(Identifier texNoPattern, Identifier texPattern)
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
	
	/** The light layer's render type for a shield with or without energy */
	public RenderType getLayerRenderType(boolean powered)
	{
		return RenderTypes.entitySolid(textureNoPattern);
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