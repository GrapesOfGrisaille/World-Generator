import org.terasology.entitySystem.Component;
import org.terasology.math.TeraMath;
import org.terasology.math.geom.BaseVector2i;
import org.terasology.math.geom.Rect2i;
import org.terasology.math.geom.Vector2f;
import org.terasology.rendering.nui.properties.Range;
import org.terasology.utilities.procedural.BrownianNoise;
import org.terasology.utilities.procedural.Noise;
import org.terasology.utilities.procedural.PerlinNoise;
import org.terasology.utilities.procedural.SubSampledNoise;
import org.terasology.world.generation.*;
import org.terasology.world.generation.facets.SurfaceHeightFacet;
import sun.java2d.Surface;

/*
 * Copyright 2019 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
@Updates(@Facet(SurfaceHeightFacet.class))
public class MntnProv implements ConfigurableFacetProvider {
    private Noise mountainNoise;
    private MountainsConfiguration configuration = new MountainsConfiguration();

    @Override
    public String getConfigurationName(){return "Mountains";}

    @Override
    public Component getConfiguration(){return configuration;}

    @Override
    public void setConfiguration(Component configuration){this.configuration = (MountainsConfiguration)configuration;}

    @Override
    public void setSeed(long seed){
        mountainNoise = new SubSampledNoise(new BrownianNoise(new PerlinNoise(seed + 2), 8), new Vector2f(0.001f, 0.001f), 1);
    }

    @Override
    public void process(GeneratingRegion region) {
        SurfaceHeightFacet facet = region.getRegionFacet(SurfaceHeightFacet.class);
        float mountainHeight = configuration.mountainHeight;
        Rect2i processRegion = facet.getWorldRegion();
        for(BaseVector2i position: processRegion.contents()){
            float additiveMountainHeight = mountainNoise.noise(position.x(), position.y()) * mountainHeight;
            additiveMountainHeight = TeraMath.clamp(additiveMountainHeight, 0, mountainHeight);
            facet.setWorld(position, facet.getWorld(position) + additiveMountainHeight);
        }
    }

    private static class MountainsConfiguration implements Component {
        @Range(min = 200, max = 500f, increment = 20f, precision = 1, description = "Mountain Height")
        private float mountainHeight = 400;
    }
}
