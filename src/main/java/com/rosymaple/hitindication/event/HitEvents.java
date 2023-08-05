package com.rosymaple.hitindication.event;

import com.rosymaple.hitindication.HitIndication;
import com.rosymaple.hitindication.latesthits.HitIndicatorType;
import com.rosymaple.hitindication.latesthits.HitMarkerType;
import com.rosymaple.hitindication.latesthits.PacketsHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.damagesource.CombatRules;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.ThrownPotion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.CriticalHitEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;
import java.util.Optional;

@Mod.EventBusSubscriber(modid = HitIndication.MODID)
public class HitEvents {
    @SubscribeEvent
    public static void onAttack(LivingDamageEvent event) {
        if(event.getSource().getDirectEntity() instanceof ThrownPotion)
            return;

        if(!(event.getSource().getEntity() instanceof LivingEntity))
            return;

        if(event.getSource().getEntity().getUUID().equals(event.getEntity().getUUID()))
            return;

        if(event.getSource().getEntity() instanceof ServerPlayer) {
            if(event.getSource().getDirectEntity() instanceof AbstractArrow)
                PacketsHelper.addHitMarker((ServerPlayer) event.getSource().getEntity(), HitMarkerType.CRIT);
        }

        if(!(event.getEntity() instanceof ServerPlayer))
            return;

        ServerPlayer player = (ServerPlayer)event.getEntity();
        LivingEntity source = (LivingEntity)event.getSource().getEntity();

        int damagePercent = (int)Math.floor((event.getAmount() / player.getMaxHealth() * 100));

        PacketsHelper.addHitIndicator(player, source, HitIndicatorType.RED, damagePercent, false);
    }

    @SubscribeEvent
    public static void onCriticalHit(CriticalHitEvent event) {
        if(!(event.getEntity() instanceof ServerPlayer) || !event.isVanillaCritical())
            return;

        ServerPlayer player = (ServerPlayer)event.getEntity();

        PacketsHelper.addHitMarker(player, HitMarkerType.CRIT);
    }

    @SubscribeEvent
    public static void onKill(LivingDeathEvent event) {
        if(!(event.getSource().getEntity() instanceof ServerPlayer))
            return;

        if(event.getSource().getEntity().getUUID().equals(event.getEntity().getUUID()))
            return;

        ServerPlayer player = (ServerPlayer)event.getSource().getEntity();

        PacketsHelper.addHitMarker(player, HitMarkerType.KILL);
    }

    @SubscribeEvent
    public static void onBlock(LivingAttackEvent event) {
        if(event.getSource().getDirectEntity() instanceof ThrownPotion)
            return;
        if(!(event.getSource().getEntity() instanceof LivingEntity))
            return;

        if(event.getSource().getEntity() instanceof ServerPlayer) {
            LivingEntity target = event.getEntity();
            ServerPlayer source = (ServerPlayer)event.getSource().getEntity();

            boolean targetIsBlocking = target.isDamageSourceBlocked(event.getSource());
            boolean shieldAboutToBreak = source.getMainHandItem().getItem().canDisableShield(source.getMainHandItem(), target.getMainHandItem(), target, source);

            if(targetIsBlocking && shieldAboutToBreak)
                PacketsHelper.addHitMarker(source, HitMarkerType.CRIT);
        }

        if(!(event.getEntity() instanceof ServerPlayer))
            return;

        ServerPlayer player = (ServerPlayer)event.getEntity();
        LivingEntity source = (LivingEntity)event.getSource().getEntity();

        boolean playerIsBlocking = player.isDamageSourceBlocked(event.getSource());
        boolean shieldAboutToBreak = source.getMainHandItem().getItem().canDisableShield(source.getMainHandItem(), player.getMainHandItem(), player, source);

        if(playerIsBlocking)
            PacketsHelper.addHitIndicator(player, source, HitIndicatorType.BLUE, shieldAboutToBreak ? 125 : 0, false);
    }

    @SubscribeEvent
    public static void onPotion(ProjectileImpactEvent event) {
        if(!(event.getProjectile().getOwner() instanceof LivingEntity)
                ||!(event.getProjectile().getOwner().getLevel() instanceof ServerLevel)
                || !(event.getProjectile() instanceof ThrownPotion))
            return;

        AABB axisalignedbb = event.getProjectile().getBoundingBox().inflate(4.0D, 2.0D, 4.0D);
        List<ServerPlayer> list = event.getProjectile().getLevel().getEntitiesOfClass(ServerPlayer.class, axisalignedbb);

        LivingEntity source = (LivingEntity)event.getProjectile().getOwner();
        ThrownPotion potion = (ThrownPotion)event.getProjectile();

        boolean hasNegativeEffects = PotionUtils.getMobEffects(potion.getItem())
                .stream().anyMatch((x) -> !x.getEffect().isBeneficial());
        boolean damagingPotion = PotionUtils.getMobEffects(potion.getItem())
                .stream().anyMatch((x) -> x.getEffect() == MobEffects.POISON
                        || x.getEffect() == MobEffects.HARM
                        || x.getEffect() == MobEffects.WITHER);

        int damagePercent = 0;
        Optional<MobEffectInstance> instantDamage = PotionUtils.getMobEffects(potion.getItem())
                .stream().filter((x) -> x.getEffect() == MobEffects.HARM).findFirst();
        for(ServerPlayer player : list) {
            if(!player.isAffectedByPotions() || player.getUUID().equals(source.getUUID()))
                continue;

            if(damagingPotion || hasNegativeEffects) {
                if(instantDamage.isPresent()) {
                    damagePercent = (int)Math.floor(getDamageAfterMagicAbsorb(player, DamageSource.MAGIC, 3*(2<<instantDamage.get().getAmplifier())) / player.getMaxHealth() * 100);
                }

                PacketsHelper.addHitIndicator(player, source, HitIndicatorType.RED, damagePercent, hasNegativeEffects && !damagingPotion);
            }
        }
    }

    protected static float getDamageAfterMagicAbsorb(ServerPlayer player, DamageSource pDamageSource, float pDamageAmount) {
        if (pDamageSource.isBypassMagic()) {
            return pDamageAmount;
        } else {
            if (player.hasEffect(MobEffects.DAMAGE_RESISTANCE) && pDamageSource != DamageSource.OUT_OF_WORLD) {
                int i = (player.getEffect(MobEffects.DAMAGE_RESISTANCE).getAmplifier() + 1) * 5;
                int j = 25 - i;
                float f = pDamageAmount * (float)j;
                float f1 = pDamageAmount;
                pDamageAmount = Math.max(f / 25.0F, 0.0F);
            }

            if (pDamageAmount <= 0.0F) {
                return 0.0F;
            } else if (pDamageSource.isBypassEnchantments()) {
                return pDamageAmount;
            } else {
                int k = EnchantmentHelper.getDamageProtection(player.getArmorSlots(), pDamageSource);
                if (k > 0) {
                    pDamageAmount = CombatRules.getDamageAfterMagicAbsorb(pDamageAmount, (float)k);
                }

                return pDamageAmount;
            }
        }
    }
}
