package edu.lapidus.rec3d.utils.image;

import org.junit.Test;

import java.awt.image.Kernel;

import static org.junit.Assert.*;

/**
 * Created by Егор on 12.05.2016.
 */
public class KernelFactoryTest {
    @Test
    public void buildOutlineKernel() throws Exception {
        Kernel k = KernelFactory.buildOutlineKernel();
        testKernel(k);
    }

    @Test
    public void buildEmbossKernel() throws Exception {
        Kernel k = KernelFactory.buildEmbossKernel();
        testKernel(k);
    }

    @Test
    public void buildSharpenKernel() throws Exception {
        Kernel k = KernelFactory.buildSharpenKernel();
        testKernel(k);
    }

    @Test
    public void buildXYGaussianKernel() throws Exception {
        Kernel k = KernelFactory.buildXYGaussianKernel(9);
        testKernel(k);
    }

    @Test
    public void buildXXGaussianKernel() throws Exception {
        Kernel k = KernelFactory.buildXXGaussianKernel(9);
        testKernel(k);
    }

    private void testKernel(Kernel k) {
        float [] s = k.getKernelData(null);
        float sum = 0;
        for (int i = 0; i < s.length; i ++) {
            sum += s[i];
        }
        assertEquals(1., sum, 0.001);
    }
}