package com.example.wojciech.program;

public class Integral
{
    private double value;

    public Integral()
    {
        value = 0;
    }

    public double integrate(double x1, double x2, double y1, double y2)
    {
        value += (y1+y2) * (x2-x1) * 0.5 * 0.001;
        return value;
    }
}
