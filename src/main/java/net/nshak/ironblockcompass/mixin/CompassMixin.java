package net.nshak.ironblockcompass.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.item.CompassItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.World;

@Mixin(CompassItem.class)
public abstract class CompassMixin extends Item {
	
	public CompassMixin(Settings settings) {
		super(settings);
	}

	@Shadow
	public static boolean hasLodestone(ItemStack stack) {return true;}
	
	@Inject(method = "Lnet/minecraft/item/CompassItem;inventoryTick(Lnet/minecraft/item/ItemStack;Lnet/minecraft/world/World;Lnet/minecraft/entity/Entity;IZ)V", at = @At("HEAD"), cancellable = true)
	private void handleInventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected, CallbackInfo ci) {
		if (!world.isClient) {
			if (hasLodestone(stack)) {
				CompoundTag compoundTag = stack.getOrCreateTag();
				if (compoundTag.contains("IronBlockTracked") && compoundTag.getBoolean("IronBlockTracked")) {
					ci.cancel();
				}
			}
		}
	}
	
	@Redirect(method = "Lnet/minecraft/item/CompassItem;useOnBlock(Lnet/minecraft/item/ItemUsageContext;)Lnet/minecraft/util/ActionResult;", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/BlockState;isOf(Lnet/minecraft/block/Block;)Z"))
	private boolean shouldTrackBlock(BlockState blockState, Block block, ItemUsageContext context) {
		if (blockState.isOf(block) || blockState.isOf(Blocks.IRON_BLOCK)) {
			CompoundTag compoundTag = context.getStack().getOrCreateTag();
			compoundTag.putBoolean("IronBlockTracked", true);
			return true;
		} else {
			return false;
		}
	}
	
	@Inject(method = "Lnet/minecraft/item/CompassItem;getTranslationKey(Lnet/minecraft/item/ItemStack;)Ljava/lang/String;", at = @At("HEAD"), cancellable = true)
	private void handleTranslationKey(ItemStack itemStack, CallbackInfoReturnable<String> cir) {
		CompoundTag compoundTag = itemStack.getTag();
		if (compoundTag != null && (compoundTag.contains("IronBlockTracked") && compoundTag.getBoolean("IronBlockTracked"))) {
			cir.setReturnValue(super.getTranslationKey(itemStack));
			cir.cancel();
		}
	}
}
