package com.example.wojciech.program;

public class ZeroVelocityUpdate
{
    /* Prog liczby probek po ktorym urzadzenie ma zostac uzane, ze jest w spoczynku */
    private final int STATIC_SAMPLES_THRESHOLD = 10;

    /* Prog wartosci przyspieszenia po ktorym urzadzenie ma zostac uzane, ze jest w spoczynku */
    private final double STATIC_ACCELERATION_THRESHOLD = 0.35;

    /* Prog wartosci przyspieszenia po ktorym urzadzenie ma zostac uzane, ze jest w spoczynku */
    private final double STATIC_ANGULAR_VELOCITY_THRESHOLD = 0.15;

    /* Zmienna odpowiedzialna za ziczanie probek */
    private int samplesCount;

    public ZeroVelocityUpdate()
    {
        samplesCount = 0;
    }

    /**
     * Funkcja odpowiedzialna za detekcje stanu spoczynkowego urzadzenia
     * @param acceleration - przyspieszenie
     * @return true jesli urzadzenie w spoczynku, false w innym wypadku
     */
    public boolean zeroVelocityUpdate(double acceleration[], double angularVelocity[])
    {
        if(Math.abs(acceleration[0]) <= STATIC_ACCELERATION_THRESHOLD &&
                Math.abs(acceleration[1]) <= STATIC_ACCELERATION_THRESHOLD &&
                Math.abs(acceleration[2]) <= STATIC_ACCELERATION_THRESHOLD &&
                Math.abs(angularVelocity[0]) <= STATIC_ANGULAR_VELOCITY_THRESHOLD &&
                Math.abs(angularVelocity[1]) <= STATIC_ANGULAR_VELOCITY_THRESHOLD &&
                Math.abs(angularVelocity[2]) <= STATIC_ANGULAR_VELOCITY_THRESHOLD)
        {
            samplesCount++;
        }
        else
        {
            samplesCount = 0;
        }

        return (samplesCount >= STATIC_SAMPLES_THRESHOLD);
    }

}
