package unity.world.blocks.power;

import arc.graphics.g2d.*;
import arc.math.*;
import arc.util.io.*;
import mindustry.content.*;
import mindustry.type.*;
import mindustry.world.consumers.*;
import unity.graphics.*;
import unity.world.blocks.*;
import unity.world.graph.*;

import static arc.Core.atlas;

public class CombustionHeater extends GenericGraphBlock{
    public final TextureRegion[] baseRegions = new TextureRegion[4];
    TextureRegion heatRegion;
    public float baseTemp = 1000 + HeatGraphNode.celsiusZero;
    public float tempPerFlammability = 1750;

    public CombustionHeater(String name){
        super(name);
        rotate = true;
    }

    @Override
    public void load(){
        super.load();
        for(int i = 0; i < 4; i++) baseRegions[i] = atlas.find(name + "-base" + (i + 1));
        heatRegion =  atlas.find(name + "-heat");
    }

    @Override
    public void init(){
        consumes.add(new ConsumeItemFilter(item -> item.flammability >= 0.1f)).update(false).optional(true, false);
        super.init();
    }

    public class CombustionHeaterBuild extends GenericGraphBuild{
        float generateTime, productionEfficiency;

        @Override
        public boolean productionValid(){
            return generateTime > 0f;
        }

        @Override
        public void update(){
            super.update();
            if(!consValid()){
                productionEfficiency = 0f;
                return;
            }

            if(generateTime <= 0f && items.total() > 0f){
                Fx.generatespark.at(x + Mathf.range(3f), y + Mathf.range(3f));
                Item item = items.take();
                productionEfficiency = item.flammability;
                generateTime = 1f;
            }



            if(generateTime > 0f){
                float am = Math.min(0.02f * delta(), generateTime);;
                if(heatNode().targetTemp<heatNode().getTemp()){
                    am *= 0.3;
                }
                generateTime -= am;
            }else{
                productionEfficiency = 0f;
            }
            heatNode().targetTemp = baseTemp + Math.max(tempPerFlammability*(productionEfficiency-1),-baseTemp*0.5f);
            heatNode().efficency = productionEfficiency==0?0:Math.min(productionEfficiency,1f/productionEfficiency);
        }

        @Override
        public void draw(){
            Draw.rect(baseRegions[rotation], x, y);
            UnityDrawf.drawHeat(heatRegion, x, y, rotdeg(), heatNode().getTemp());

            drawTeamTop();
        }

        @Override
        public void write(Writes write){
            super.write(write);
            write.f(productionEfficiency);
        }

        @Override
        public void read(Reads read, byte revision){
            super.read(read,revision);
            productionEfficiency = read.f();
        }
    }
}