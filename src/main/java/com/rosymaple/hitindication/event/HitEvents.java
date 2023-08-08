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

        if(!(event.getSource().getEntity() instanceof LivingEntity)
                || event.getSource().getEntity().getUUID().equals(event.getEntity().getUUID())) {

            if(!(event.getEntity() instanceof ServerPlayer))
                return;

            ServerPlayer player = (ServerPlayer)event.getEntity();
            int damagePercent = (int)Math.floor((event.getAmount() / player.getMaxHealth() * 100));

            PacketsHelper.addHitIndicator(player, null, HitIndicatorType.ND_RED, damagePercent, false);
            return;
        }

        if(event.getSource().getEntity() instanceof ServerPlayer) {
            if(event.getSource().getDirectEntity() instanceof AbstractArrow)
                PacketsHelper.addHitMarker((ServerPlayer) event.getSource().getEntity(), HitMarkerType.CRIT);
        }

        if(!(event.getEntityLiving() instanceof ServerPlayer))
            return;

        ServerPlayer player = (ServerPlayer)event.getEntityLiving();
        LivingEntity source = (LivingEntity)event.getSource().getEntity();

        int damagePercent = (int)Math.floor((event.getAmount() / player.getMaxHealth() * 100));

        PacketsHelper.addHitIndicator(player, source, HitIndicatorType.RED, damagePercent, false);
    }

    @SubscribeEvent
    public static void onCriticalHit(CriticalHitEvent event) {
        if(!(event.getPlayer() instanceof ServerPlayer) || !event.isVanillaCritical())
            return;

        ServerPlayer player = (ServerPlayer)event.getPlayer();

        PacketsHelper.addHitMarker(player, HitMarkerType.CRIT);
    }

    @SubscribeEvent
    public static void onKill(LivingDeathEvent event) {
        if(!(event.getSource().getEntity() instanceof ServerPlayer))
            return;

        if(event.getSource().getEntity().getUUID().equals(event.getEntityLiving().getUUID()))
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
            LivingEntity target = event.getEntityLiving();
            ServerPlayer source = (ServerPlayer)event.getSource().getEntity();

            boolean targetIsBlocking = canBlockDamageSource(target, event.getSource());
            boolean shieldAboutToBreak = source.getMainHandItem().getItem().canDisableShield(source.getMainHandItem(), target.getMainHandItem(), target, source);

            if(targetIsBlocking && shieldAboutToBreak)
                PacketsHelper.addHitMarker(source, HitMarkerType.CRIT);
        }

        if(!(event.getEntityLiving() instanceof ServerPlayer))
            return;

        ServerPlayer player = (ServerPlayer)event.getEntityLiving();
        LivingEntity source = (LivingEntity)event.getSource().getEntity();

        boolean playerIsBlocking = canBlockDamageSource(player, event.getSource());
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
                    damagePercent = (int)Math.floor(applyPotionDamageCalculations(player, DamageSource.MAGIC, 3*(2<<instantDamage.get().getAmplifier())) / player.getMaxHealth() * 100);
                }

                PacketsHelper.addHitIndicator(player, source, HitIndicatorType.RED, damagePercent, hasNegativeEffects && !damagingPotion);
            }
        }
    }

    private static boolean canBlockDamageSource(LivingEntity player, DamageSource pDamageSource)
    {
        Entity entity = pDamageSource.getDirectEntity();
        boolean flag = false;
        if (entity instanceof AbstractArrow) {
            AbstractArrow abstractarrow = (AbstractArrow)entity;
            if (abstractarrow.getPierceLevel() > 0) {
                flag = true;
            }
        }

        if (!pDamageSource.isBypassArmor() && player.isBlocking() && !flag) {
            Vec3 vec32 = pDamageSource.getSourcePosition();
            if (vec32 != null) {
                Vec3 vec3 = player.getViewVector(1.0F);
                Vec3 vec31 = vec32.vectorTo(player.position()).normalize();
                vec31 = new Vec3(vec31.x, 0.0D, vec31.z);
                if (vec31.dot(vec3) < 0.0D) {
                    return true;
                }
            }
        }

        return false;
    }

    private static float applyPotionDamageCalculations(ServerPlayer player, DamageSource pSource, float pDamage)
    {
        if (pSource.isBypassMagic()) {
            return pDamage;
        } else {
            if (player.hasEffect(MobEffects.DAMAGE_RESISTANCE) && pSource != DamageSource.OUT_OF_WORLD) {
                int i = (player.getEffect(MobEffects.DAMAGE_RESISTANCE).getAmplifier() + 1) * 5;
                int j = 25 - i;
                float f = pDamage * (float)j;
                float f1 = pDamage;
                pDamage = Math.max(f / 25.0F, 0.0F);
                float f2 = f1 - pDamage;
                if (f2 > 0.0F && f2 < 3.4028235E37F) {
                    if (player instanceof ServerPlayer) {
                        ((ServerPlayer)player).awardStat(Stats.CUSTOM.get(Stats.DAMAGE_RESISTED), Math.round(f2 * 10.0F));
                    } else if (pSource.getEntity() instanceof ServerPlayer) {
                        ((ServerPlayer)pSource.getEntity()).awardStat(Stats.CUSTOM.get(Stats.DAMAGE_DEALT_RESISTED), Math.round(f2 * 10.0F));
                    }
                }
            }

            if (pDamage <= 0.0F) {
                return 0.0F;
            } else {
                int k = EnchantmentHelper.getDamageProtection(player.getArmorSlots(), pSource);
                if (k > 0) {
                    pDamage = CombatRules.getDamageAfterMagicAbsorb(pDamage, (float)k);
                }

                return pDamage;
            }
        }
    }


}
