package com.simibubi.create.compat.griefdefender;


import java.util.Objects;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class GriefDefenderUtils {

	public static Object gdCore;
	public static Class trustTypeClass;
	public static Class trustTypesClass;
	private static boolean isEnabled;
	static {
		try {
			Plugin GriefDefenderBPL = Bukkit.getPluginManager().getPlugin("GriefDefender");
			ClassLoader PluginClassLoader = GriefDefenderBPL.getClass().getClassLoader();
			Class GriefDefenderAPIClass = PluginClassLoader.loadClass("com.griefdefender.api.GriefDefender");
			trustTypesClass = PluginClassLoader.loadClass("com.griefdefender.api.claim.TrustTypes");
			trustTypeClass = PluginClassLoader.loadClass("com.griefdefender.api.claim.TrustType");
			gdCore = GriefDefenderAPIClass.getMethod("getCore").invoke(null);
			isEnabled = true;
			System.out.println("GriefDefender Compat is not active");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static boolean isEnabled() {
		return isEnabled;
	}

	public static boolean canInteract(Level world, BlockPos pos1, BlockPos pos2) {
		if (!isEnabled()) return true;
		return Objects.equals(getRegionUUID(world, pos1), getRegionUUID(world, pos2));
	}

	public static UUID getRegionUUID(Level world, BlockPos pos) {
		String worldName = getWorldName(world.dimension());
		UUID worldID = Bukkit.getServer().getWorld(worldName).getUID();
		try {
			Object claim = gdCore.getClass().getMethod("getClaimAt", UUID.class, int.class, int.class, int.class)
					.invoke(gdCore,worldID, pos.getX(), pos.getY(), pos.getZ());
			UUID result = (UUID) claim.getClass().getMethod("getUniqueId").invoke(claim);
			return result;
		}catch (Exception e){
			e.printStackTrace();
		}
		return UUID.randomUUID();
	}

	public static boolean canUse(Player player, Level world, BlockPos pos) {
		if (!isEnabled()) return true;
		String worldName = getWorldName(world.dimension());
		UUID worldID = Bukkit.getServer().getWorld(worldName).getUID();
		try {
			Object claim = gdCore.getClass().getMethod("getClaimAt", UUID.class, int.class, int.class, int.class)
					.invoke(gdCore,worldID, pos.getX(), pos.getY(), pos.getZ());

			Object trustType = trustTypesClass.getField("BUILDER").get(null);
			boolean result = (boolean) claim.getClass().getMethod("isUserTrusted",UUID.class,trustTypeClass).invoke(claim,player.getUUID(),trustType);
			result = result || (boolean)claim.getClass().getMethod("isWilderness").invoke(claim);
			return result;
		}catch (Exception e){
			e.printStackTrace();
		}
		return false;
	}

	public static String getWorldName(ResourceKey<Level> pDimensionKey) {
		if (pDimensionKey == Level.OVERWORLD) {
			return "world";
		} else if (pDimensionKey == Level.END) {
			return "world/DIM1";
		} else {
			return pDimensionKey == Level.NETHER ? "world/DIM-1" : "world/dimensions"+pDimensionKey.location().getNamespace()+"/"+pDimensionKey.location().getPath();
		}
	}
}
