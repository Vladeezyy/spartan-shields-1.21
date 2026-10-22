package com.oblivioussp.spartanshields.client.model;

import java.util.IdentityHashMap;
import java.util.Map;

import org.joml.Matrix4f;
import org.joml.Vector3f;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.oblivioussp.spartanshields.util.Log;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;

/**
 * The light layer of a tower shield (Mekanist, Dark Steel) drawn a second time over the whole model, but only with the
 * faces that show something in the light textures: those textures light up 2-9 % of their area, the other faces were
 * drawn fully transparent (translucent "lights off") or black (additive "lights on"), which leaves no trace. The faces
 * are picked once per resource reload; the picture is the same.
 *
 * <p>If the light textures cannot be read, the whole model is drawn as before.
 */
public final class GlowLayer
{
	/** Per cube: the visible faces, packed as normal (3) then 4 vertices of position (3) and uv (2) */
	private final Map<ModelPart.Cube, float[]> faces = new IdentityHashMap<>();
	private int total, kept;

	private GlowLayer() {}

	/**
	 * @param textures the light textures; {@code additive[i]}: black counts as unlit (eyes render type), otherwise
	 * only full transparency does
	 */
	public static GlowLayer of(ModelPart root, ResourceManager manager, Identifier[] textures, boolean[] additive)
	{
		try
		{
			boolean[][] lit = null;
			int width = 0, height = 0;
			for(int t = 0; t < textures.length; t++)
			{
				try(NativeImage image = NativeImage.read(manager.open(textures[t])))
				{
					if(lit == null)
					{
						width = image.getWidth();
						height = image.getHeight();
						lit = new boolean[width][height];
					}
					for(int x = 0; x < Math.min(width, image.getWidth()); x++)
						for(int y = 0; y < Math.min(height, image.getHeight()); y++)
						{
							int argb = image.getPixel(x, y);
							boolean visible = (argb >>> 24) != 0 && (!additive[t] || (argb & 0xFFFFFF) != 0);
							lit[x][y] |= visible;
						}
				}
			}
			GlowLayer layer = new GlowLayer();
			boolean[][] mask = lit;
			int w = width, h = height;
			root.visit(new PoseStack(), (pose, path, index, cube) -> layer.add(cube, mask, w, h));
			Log.info("Shield light layer " + textures[0] + ": " + layer.kept + " of " + layer.total + " faces");
			return layer;
		}
		catch(Exception | LinkageError e)
		{
			Log.error("Could not prepare a shield light layer, it is drawn whole: " + e);
			return null;
		}
	}

	private void add(ModelPart.Cube cube, boolean[][] mask, int width, int height)
	{
		// 1.21.11: the faces and their vertices are public records (1.21.1 needed reflection)
		float[] packed = new float[cube.polygons.length * 23];
		int n = 0;
		for(ModelPart.Polygon polygon : cube.polygons)
		{
			total++;
			ModelPart.Vertex[] vertices = polygon.vertices();
			float minU = 1, minV = 1, maxU = 0, maxV = 0;
			for(int i = 0; i < 4; i++)
			{
				minU = Math.min(minU, vertices[i].u());
				maxU = Math.max(maxU, vertices[i].u());
				minV = Math.min(minV, vertices[i].v());
				maxV = Math.max(maxV, vertices[i].v());
			}
			if(!anyLit(mask, width, height, minU, minV, maxU, maxV))
				continue;
			kept++;
			packed[n++] = polygon.normal().x();
			packed[n++] = polygon.normal().y();
			packed[n++] = polygon.normal().z();
			for(int i = 0; i < 4; i++)
			{
				packed[n++] = vertices[i].x();
				packed[n++] = vertices[i].y();
				packed[n++] = vertices[i].z();
				packed[n++] = vertices[i].u();
				packed[n++] = vertices[i].v();
			}
		}
		if(n > 0)
			faces.put(cube, java.util.Arrays.copyOf(packed, n));
	}

	private static boolean anyLit(boolean[][] mask, int width, int height, float minU, float minV, float maxU, float maxV)
	{
		int x0 = Math.max(0, (int)Math.floor(minU * width)), x1 = Math.min(width, (int)Math.ceil(maxU * width));
		int y0 = Math.max(0, (int)Math.floor(minV * height)), y1 = Math.min(height, (int)Math.ceil(maxV * height));
		for(int x = x0; x < x1; x++)
			for(int y = y0; y < y1; y++)
				if(mask[x][y])
					return true;
		return false;
	}

	/** Same vertices as {@code ModelPart.render}, only for the visible faces */
	public void render(ModelPart root, PoseStack poseStack, VertexConsumer consumer, int packedLight, int packedOverlay, int color)
	{
		Vector3f normal = new Vector3f(), position = new Vector3f();
		root.visit(poseStack, (pose, path, index, cube) ->
		{
			float[] data = faces.get(cube);
			if(data == null)
				return;
			Matrix4f matrix = pose.pose();
			for(int i = 0; i < data.length; i += 23)
			{
				pose.transformNormal(data[i], data[i + 1], data[i + 2], normal);
				for(int v = 0; v < 4; v++)
				{
					int o = i + 3 + v * 5;
					matrix.transformPosition(data[o] / 16.0f, data[o + 1] / 16.0f, data[o + 2] / 16.0f, position);
					consumer.addVertex(position.x(), position.y(), position.z(), color, data[o + 3], data[o + 4], packedOverlay, packedLight,
							normal.x(), normal.y(), normal.z());
				}
			}
		});
	}
}
