package com.rosymaple.hitindication.event;

import com.rosymaple.hitindication.HitIndication;
import com.rosymaple.hitindication.latesthits.HitIndicatorType;
import com.rosymaple.hitindication.latesthits.HitMarkerType;
import com.rosymaple.hitindication.latesthits.PacketsHelper;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IProjectile;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.projectile.EntityPotion;
import net.minecraft.init.MobEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.potion.PotionUtils;
import net.minecraft.util.CombatRules;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.WorldServer;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.CriticalHitEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.List;
import java.util.Optional;

@Mod.EventBusSubscriber(modid = HitIndication.MODID)
public class HitEvents {
    @SubscribeEvent
    public static void onAttack(LivingDamageEvent event) {
        Entity attackerProjectile = event.getSource().getImmediateSource();
        Entity attacker = event.getSource().getTrueSource();
        Entity target = event.getEntity();
        if(attackerProjectile instanceof EntityPotion)
            return;

        if(attacker instanceof EntityPlayerMP)
            if(attackerProjectile instanceof IProjectile && !attacker.getUniqueID().equals(target.getUniqueID()))
                PacketsHelper.addHitMarker((EntityPlayerMP)attacker, HitMarkerType.CRIT);

        if(!(target instanceof EntityPlayerMP))
            return;

        EntityPlayerMP targetPlayer = (EntityPlayerMP)target;
        int damagePercent = (int)Math.floor((event.getAmount() / targetPlayer.getMaxHealth() * 100));
        if(!(attacker instanceof EntityLivingBase) || attacker.getUniqueID().equals(target.getUniqueID()))
            PacketsHelper.addHitIndicator(targetPlayer, null, HitIndicatorType.ND_HIT, damagePercent, false);
        else
            PacketsHelper.addHitIndicator(targetPlayer, (EntityLivingBase)attacker, HitIndicatorType.HIT, damagePercent, false);
    }

    @SubscribeEvent
    public static void onCriticalHit(CriticalHitEvent event) {
        if(!(event.getEntityPlayer() instanceof EntityPlayerMP) || !event.isVanillaCritical())
            return;

        EntityPlayerMP player = (EntityPlayerMP)event.getEntityPlayer();
        PacketsHelper.addHitMarker(player, HitMarkerType.CRIT);
    }

    @SubscribeEvent
    public static void onKill(LivingDeathEvent event) {
        Entity attacker = event.getSource().getTrueSource();
        Entity target = event.getEntity();

        if(!(attacker instanceof EntityPlayerMP))
            return;
        if(attacker.getUniqueID().equals(target.getUniqueID()))
            return;

        PacketsHelper.addHitMarker((EntityPlayerMP)attacker, HitMarkerType.KILL);
    }

    @SubscribeEvent
    public static void onBlock(LivingAttackEvent event) {
        Entity attackerProjectile = event.getSource().getImmediateSource();
        Entity attacker = event.getSource().getTrueSource();
        EntityLivingBase target = event.getEntityLiving();
        if(attackerProjectile instanceof EntityPotion)
            return;
        if(!(attacker instanceof EntityLivingBase))
            return;

        EntityLivingBase livingAttacker = (EntityLivingBase)attacker;

        ItemStack itemInHand = livingAttacker.getHeldItemMainhand();
        boolean targetIsBlocking = canBlockDamageSource(target, event.getSource());
        boolean shieldAboutToBreak = itemInHand.getItem().canDisableShield(itemInHand, target.getActiveItemStack(), target, livingAttacker);
        if(targetIsBlocking) {
            if(target instanceof EntityPlayerMP) {
                EntityPlayerMP targetPlayer = (EntityPlayerMP)target;
                PacketsHelper.addHitIndicator(targetPlayer, livingAttacker, HitIndicatorType.BLOCK, shieldAboutToBreak ? 125 : 0, false);
            }

            if(attacker instanceof EntityPlayerMP && shieldAboutToBreak) {
                EntityPlayerMP attackingPlayer = (EntityPlayerMP)attacker;
                PacketsHelper.addHitMarker(attackingPlayer, HitMarkerType.CRIT);
            }
        }
    }

    @SubscribeEvent
    public static void onPotion(ProjectileImpactEvent.Throwable event) {
        if(event.getThrowable().getThrower() == null
                ||!(event.getThrowable().getThrower().getEntityWorld() instanceof WorldServer)
                || !(event.getThrowable() instanceof EntityPotion))
            return;

        AxisAlignedBB axisalignedbb = event.getThrowable().getEntityBoundingBox().grow(4.0D, 2.0D, 4.0D);
        List<EntityPlayerMP> list = event.getThrowable().world.getEntitiesWithinAABB(EntityPlayerMP.class, axisalignedbb);

        EntityLivingBase source = event.getThrowable().getThrower();
        EntityPotion potion = (EntityPotion)event.getThrowable();

        boolean hasNegativeEffects = PotionUtils.getEffectsFromStack(potion.getPotion())
                .stream().anyMatch((x) -> x.getPotion().isBadEffect());
        boolean damagingPotion = PotionUtils.getEffectsFromStack(potion.getPotion())
                .stream().anyMatch((x) -> x.getPotion() == MobEffects.POISON
                        || x.getPotion() == MobEffects.INSTANT_DAMAGE
                        || x.getPotion() == MobEffects.WITHER);

        int damagePercent = 0;
        Optional<PotionEffect> instantDamage = PotionUtils.getEffectsFromStack(potion.getPotion())
                .stream().filter((x) -> x.getPotion() == MobEffects.INSTANT_DAMAGE).findFirst();
        for(EntityPlayerMP player : list) {
            if(!player.canBeHitWithPotion() || player.getUniqueID().equals(source.getUniqueID()))
                continue;

            if(damagingPotion || hasNegativeEffects) {
                if(instantDamage.isPresent()) {
                    damagePercent = (int)Math.floor(applyPotionDamageCalculations(player, DamageSource.MAGIC, 3*(2<<instantDamage.get().getAmplifier())) / player.getMaxHealth() * 100);
                }

                PacketsHelper.addHitIndicator(player, source, HitIndicatorType.HIT, damagePercent, hasNegativeEffects && !damagingPotion);
            }
        }
    }

    // EntityLivingBase.canBlockDamageSource
    private static boolean canBlockDamageSource(EntityLivingBase entity, DamageSource damageSourceIn) {
        if (!damageSourceIn.isUnblockable() && entity.isActiveItemStackBlocking()) {
            Vec3d vec3d = damageSourceIn.getDamageLocation();
            if (vec3d != null) {
                Vec3d vec3d1 = entity.getLook(1.0F);
                Vec3d vec3d2 = vec3d.subtractReverse(new Vec3d(entity.posX, entity.posY, entity.posZ)).normalize();
                vec3d2 = new Vec3d(vec3d2.x, 0.0D, vec3d2.z);

                return vec3d2.dotProduct(vec3d1) < 0.0D;
            }
        }

        return false;
    }

    // Player.applyPotionDamageCalculations
    private static float applyPotionDamageCalculations(EntityPlayerMP player, DamageSource source, float damage) {
        if (source.isDamageAbsolute())
            return damage;

        if (player.isPotionActive(MobEffects.RESISTANCE) && source != DamageSource.OUT_OF_WORLD) {
            int i = (player.getActivePotionEffect(MobEffects.RESISTANCE).getAmplifier() + 1) * 5;
            int j = 25 - i;
            float f = damage * (float)j;
            damage = f / 25.0F;
        }

        if (damage <= 0.0F)
            return 0.0F;

        int k = EnchantmentHelper.getEnchantmentModifierDamage(player.getArmorInventoryList(), source);
        if (k > 0)
            damage = CombatRules.getDamageAfterMagicAbsorb(damage, (float)k);

        return damage;
    }
}
