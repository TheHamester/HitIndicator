package com.rosymaple.hitindication.event;

import com.rosymaple.hitindication.HitIndication;
import com.rosymaple.hitindication.capability.latesthits.Indicator;
import com.rosymaple.hitindication.capability.latesthits.LatestHitsProvider;
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
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.entity.projectile.ThrownPotion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;
import java.util.Optional;

@Mod.EventBusSubscriber(modid = HitIndication.MODID)
public class HitEvents {
    @SubscribeEvent
    public static void onAttack(LivingDamageEvent event) {
        System.out.println(event.getSource().getDirectEntity());
        if(!(event.getEntityLiving() instanceof ServerPlayer)
                || !(event.getSource().getEntity() instanceof LivingEntity)
                || event.getSource().getEntity() instanceof ThrownPotion)
            return;

        ServerPlayer player = (ServerPlayer)event.getEntityLiving();
        LivingEntity source = (LivingEntity)event.getSource().getEntity();

        int damagePercent = (int)Math.floor((event.getAmount() / player.getMaxHealth() * 100));

        player.getCapability(LatestHitsProvider.LATEST_HITS, null).ifPresent((hits) -> {
            hits.addHit(player, source, Indicator.RED, damagePercent, false);
        });
    }

    @SubscribeEvent
    public static void onBlock(LivingAttackEvent event) {
        if(!(event.getEntityLiving() instanceof ServerPlayer)
                || !(event.getSource().getEntity() instanceof LivingEntity)
                || event.getSource().getEntity() instanceof ThrownPotion)
            return;

        ServerPlayer player = (ServerPlayer)event.getEntityLiving();
        LivingEntity source = (LivingEntity)event.getSource().getEntity();

        player.getCapability(LatestHitsProvider.LATEST_HITS, null).ifPresent((hits) -> {
            boolean playerIsBlocking = canBlockDamageSource(player, event.getSource());
            boolean shieldAboutToBreak = source.getMainHandItem().getItem().canDisableShield(source.getMainHandItem(), player.getMainHandItem(), player, source);
            if(playerIsBlocking) {
                hits.addHit(player, source, Indicator.BLUE, shieldAboutToBreak ? 125 : 0, false);
            }
        });
    }

    @SubscribeEvent
    public static void onPotion(ProjectileImpactEvent event) {
        if(!(event.getProjectile() instanceof ThrowableProjectile)
                || !(event.getProjectile().getOwner() instanceof LivingEntity)
                || !(event.getProjectile().getOwner().getLevel() instanceof ServerLevel)
                || !(event.getProjectile() instanceof ThrownPotion))
            return;

        AABB axisalignedbb = event.getProjectile().getBoundingBox().inflate(4.0D, 2.0D, 4.0D);
        List<ServerPlayer> list = event.getProjectile().level.getEntitiesOfClass(ServerPlayer.class, axisalignedbb);

        LivingEntity source = (LivingEntity)event.getProjectile().getOwner();
        ThrownPotion potion = (ThrownPotion)event.getProjectile();

        boolean hasNegativeEffects = PotionUtils.getMobEffects(potion.getItem())
                .stream().anyMatch((x) -> !x.getEffect().isBeneficial());
        boolean damagingPotion = PotionUtils.getMobEffects(potion.getItem())
                .stream().anyMatch((x) -> x.getEffect() == MobEffects.POISON
                        || x.getEffect() == MobEffects.HARM
                        || x.getEffect() == MobEffects.WITHER);

        Optional<MobEffectInstance> instantDamage = PotionUtils.getMobEffects(potion.getItem())
                .stream().filter((x) -> x.getEffect() == MobEffects.HARM).findFirst();
        for(ServerPlayer player : list) {
            if(!player.isAffectedByPotions())
                continue;

            player.getCapability(LatestHitsProvider.LATEST_HITS, null).ifPresent((hits) -> {
                int damagePercent = 0;
                if(damagingPotion || hasNegativeEffects) {
                    if(instantDamage.isPresent()) {
                        damagePercent = (int)Math.floor(applyPotionDamageCalculations(player, DamageSource.MAGIC, 3*(2<<instantDamage.get().getAmplifier())) / player.getMaxHealth() * 100);
                    }

                    hits.addHit(player, source, Indicator.RED, damagePercent, hasNegativeEffects && !damagingPotion);
                }
            });
        }
    }

    @SubscribeEvent
    public static void onTick(TickEvent.PlayerTickEvent event) {
        if(!(event.player instanceof ServerPlayer) || event.phase == TickEvent.Phase.END)
            return;

        event.player.getCapability(LatestHitsProvider.LATEST_HITS, null).ifPresent((hits) -> {
            hits.tick((ServerPlayer)event.player);
        });
    }

    private static boolean canBlockDamageSource(ServerPlayer player, DamageSource pDamageSource)
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
