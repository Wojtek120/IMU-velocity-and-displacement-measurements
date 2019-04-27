package com.example.wojciech.program;


public class GravityCompensation
{
        private static final double GRAVITY = 9.91;

        public static double[] CompensateGravity(double acc[], double q[])
        {
            double g[] = new double[3];

            g[0] = 2 * (q[1] * q[3] - q[0] * q[2]) * GRAVITY;
            g[1] = 2 * (q[0] * q[1] + q[2] * q[3]) * GRAVITY;
            g[2] = (q[0] * q[0] - q[1] * q[1] - q[2] * q[2] + q[3] * q[3]) * GRAVITY;

            return new double[]{acc[0] - g[0], acc[1] - g[1], acc[2] - g[2]};
        }
}
