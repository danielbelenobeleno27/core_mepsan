package com.core.app.protocols;

public interface Base {

    public String getCheckSum();

    /**
     *
     * @param trama
     * @return
     */
    byte[] calcularCheckSum(byte[] trama);

    public void setCara(int cara);

    public void setArr(String[] arr);

}
