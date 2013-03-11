package icbm.zhapin.render;

import icbm.core.ZhuYao;
import icbm.zhapin.jiqi.TYinDaoQi;
import icbm.zhapin.muoxing.jiqi.MYinDaoQi;

import java.util.List;

import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;

import org.lwjgl.opengl.GL11;

public class RYinDaoQi extends TileEntitySpecialRenderer
{
	public static final String TEXTURE_FILE = "missile_coordinator_off.png";
	public static final String TEXTURE_FILE_2 = "missile_coordinator_on.png";
	public static final MYinDaoQi MODEL = new MYinDaoQi();

	public void renderModelAt(TYinDaoQi tileEntity, double x, double y, double z, float f)
	{
		GL11.glPushMatrix();
		GL11.glTranslatef((float) x + 0.5F, (float) y + 1.5F, (float) z + 0.5F);

		int radius = 2;
		List players = tileEntity.worldObj.getEntitiesWithinAABB(EntityPlayer.class, AxisAlignedBB.getBoundingBox(tileEntity.xCoord - radius, tileEntity.yCoord - radius, tileEntity.zCoord - radius, tileEntity.xCoord + radius, tileEntity.yCoord + radius, tileEntity.zCoord + radius));

		if (players.size() > 0)
		{
			this.bindTextureByName(ZhuYao.TEXTURE_PATH + TEXTURE_FILE_2);
		}
		else
		{
			this.bindTextureByName(ZhuYao.TEXTURE_PATH + TEXTURE_FILE);
		}

		GL11.glRotatef(180F, 0.0F, 0.0F, 1.0F);

		switch (tileEntity.getDirection(tileEntity.worldObj, (int) x, (int) y, (int) z).ordinal())
		{
			case 2:
				GL11.glRotatef(180F, 0.0F, 180F, 1.0F);
				break;
			case 4:
				GL11.glRotatef(90F, 0.0F, 180F, 1.0F);
				break;
			case 5:
				GL11.glRotatef(-90F, 0.0F, 180F, 1.0F);
				break;
		}

		MODEL.render(0, 0.0625F);
		GL11.glPopMatrix();
	}

	@Override
	public void renderTileEntityAt(TileEntity tileentity, double d, double d1, double d2, float f)
	{
		renderModelAt((TYinDaoQi) tileentity, d, d1, d2, f);
	}
}