package com.emistoolbox.client;

public abstract interface EmisEditor<T>
{
    public abstract void commit();

    public abstract T get();

    public abstract void set(T paramT);
}
