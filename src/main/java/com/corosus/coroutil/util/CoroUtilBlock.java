package com.corosus.coroutil.util;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;

public class CoroUtilBlock {
	
	public static boolean isAir(Block parBlock) {
		Material mat = parBlock.defaultBlockState().getMaterial();
		if (mat == Material.AIR) {
			return true;
		} else {
			return false;
		}
	}
	
}
