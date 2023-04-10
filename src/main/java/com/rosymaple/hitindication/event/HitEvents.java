package com.rosymaple.hitindication.event;

import com.rosymaple.hitindication.HitIndication;
import com.rosymaple.hitindication.capability.latesthits.Indicator;
import com.rosymaple.hitindication.capability.latesthits.LatestHits;
import com.rosymaple.hitindication.capability.latesthits.LatestHitsProvider;
import com.rosymaple.hitindication.config.HitIndicatorConfig;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.projectile.EntityPotion;
import net.minecraft.init.MobEffects;
import net.minecraft.potion.PotionUtils;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.WorldServer;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.List;

@Mod.EventBusSubscriber(modid = HitIndication.MODID)
public class HitEvents {
    @SubscribeEvent
    public static void onAttack(LivingHurtEvent event) {
        if(!(event.getEntityLiving() instanceof EntityPlayerMP)
                || !(event.getSource().getTrueSource() instanceof EntityLiving)
                || event.getSource().getImmediateSource() instanceof EntityPotion)
            return;

        EntityPlayerMP player = (EntityPlayerMP)event.getEntityLiving();
        EntityLiving source = (EntityLiving)event.getSource().getTrueSource();

        LatestHits hits = player.getCapability(LatestHitsProvider.LATEST_HITS, null);
        if(hits == null)
            return;

        boolean playerIsBlocking = canBlockDamageSource(player, event.getSource());
        if(playerIsBlocking && HitIndicatorConfig.ShowBlueIndicators) {
            hits.addHit(player, source, Indicator.BLUE);
            return;
        }

        hits.addHit(player, source, Indicator.RED);
    }

    @SubscribeEvent
    public static void onPotion(ProjectileImpactEvent.Throwable event) {
        if(!(event.getThrowable().getThrower() instanceof EntityLiving)
                ||!(event.getThrowable().getThrower().getEntityWorld() instanceof WorldServer)
                || !(event.getThrowable() instanceof EntityPotion))
            return;

        AxisAlignedBB axisalignedbb = event.getThrowable().getEntityBoundingBox().grow(4.0D, 2.0D, 4.0D);
        List<EntityPlayerMP> list = event.getThrowable().world.getEntitiesWithinAABB(EntityPlayerMP.class, axisalignedbb);

        EntityLiving source = (EntityLiving)event.getThrowable().getThrower();
        EntityPotion potion = (EntityPotion)event.getThrowable();

        boolean hasNegativeEffects = PotionUtils.getEffectsFromStack(potion.getPotion())
                .stream().anyMatch((x) -> x.getPotion().isBadEffect());
        boolean damagingPotion = PotionUtils.getEffectsFromStack(potion.getPotion())
                .stream().anyMatch((x) -> x.getPotion() == MobEffects.POISON
                        || x.getPotion() == MobEffects.INSTANT_DAMAGE
                        || x.getPotion() == MobEffects.WITHER);

        for(EntityPlayerMP player : list) {
            if(!player.canBeHitWithPotion())
                continue;

            LatestHits hits = player.getCapability(LatestHitsProvider.LATEST_HITS, null);
            if (hits == null)
                continue;

            if(damagingPotion || (hasNegativeEffects && HitIndicatorConfig.DisplayHitsFromNegativePotions))
                hits.addHit(player, source, Indicator.RED);

        }
    }

    @SubscribeEvent
    public static void onTick(TickEvent.PlayerTickEvent event) {
        if(!(event.player instanceof EntityPlayerMP) || event.phase == TickEvent.Phase.END)
            return;

        LatestHits hits = event.player.getCapability(LatestHitsProvider.LATEST_HITS, null);
        if(hits != null) {
            hits.tick((EntityPlayerMP)event.player);
        }
    }

    private static boolean canBlockDamageSource(EntityPlayerMP entity, DamageSource damageSourceIn)
    {
        if (!damageSourceIn.isUnblockable() && entity.isActiveItemStackBlocking())
        {
            Vec3d vec3d = damageSourceIn.getDamageLocation();

            if (vec3d != null)
            {
                Vec3d vec3d1 = entity.getLook(1.0F);
                Vec3d vec3d2 = vec3d.subtractReverse(new Vec3d(entity.posX, entity.posY, entity.posZ)).normalize();
                vec3d2 = new Vec3d(vec3d2.x, 0.0D, vec3d2.z);

                if (vec3d2.dotProduct(vec3d1) < 0.0D)
                {
                    return true;
                }
            }
        }

        return false;
    }


}
