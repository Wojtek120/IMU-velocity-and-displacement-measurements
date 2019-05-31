package com.example.wojciech.program;

/**
 * Klasa, w ktorej liczona jest calka
 */
public class Integral
{
    /**
     * Aktualana wartosc calki
     */
    private double value;

    public Integral()
    {
        value = 0;
    }

    /**
     * Calkowanie
     * @param x1 - wspolrzedna x pierwszego punktu
     * @param x2 - wspolrzedna x drugiego punktu
     * @param y1 - wspolrzedna y pierwszego punktu
     * @param y2 - wspolrzedna y drugiego punktu
     * @return aktualna wartosc calki
     */
    public double integrate(double x1, double x2, double y1, double y2)
    {
        value += (y1+y2) * (x2-x1) * 0.5 * 0.001;
        return value;
    }

    public void setToZero()
    {
        value = 0;
    }
}
