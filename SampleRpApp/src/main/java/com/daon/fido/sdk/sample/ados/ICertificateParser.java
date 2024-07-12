package com.daon.fido.sdk.sample.ados;

/**
 * Used when parsing the ADoS root certificate
 */
public interface ICertificateParser {
    /**
     *
     * @param pemEncodedCert pem formatted cert
     * @return base-64 cert data
     */
    String parse(String pemEncodedCert);

    /**
     *
     * @param cert pem or der formatted cert
     * @return base-64 cert data
     */
    String parse(byte[] cert);
}
