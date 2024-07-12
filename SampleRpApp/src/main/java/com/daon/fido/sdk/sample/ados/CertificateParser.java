package com.daon.fido.sdk.sample.ados;

import android.util.Base64;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Scanner;

public class CertificateParser implements ICertificateParser {
    private static final String[] PEM_CERT_PREFIXES = new String[] {
            "-----BEGIN CERTIFICATE-----",
            "-----BEGIN X509 CERTIFICATE-----",
            "-----BEGIN TRUSTED CERTIFICATE-----"
    };
    private static final String[] PEM_CERT_POSTFIXES = new String[] {
            "-----END CERTIFICATE-----",
            "-----END X509 CERTIFICATE-----",
            "-----END TRUSTED CERTIFICATE-----"
    };
    private static final int MIN_VALID_PEM_LENGTH = 200;
    private static final int MAX_VALID_PEM_LENGTH = 30000;
    private static final int MIN_VALID_CERT_LENGTH = 130;
    private static final int MAX_VALID_CERT_LENGTH = 20000;

    @Override
    public String parse(String pemEncodedCert) {
        if(pemEncodedCert==null || pemEncodedCert.length() < MIN_VALID_PEM_LENGTH || pemEncodedCert.length() > MAX_VALID_PEM_LENGTH) {
            throw new IllegalArgumentException("Invalid certificate format.");
        }
        Scanner scanner = new Scanner(pemEncodedCert);
        StringBuilder stringBuilder = new StringBuilder();
        boolean firstLine = true;
        String line = null;
        while (scanner.hasNextLine()) {
            line = scanner.nextLine();
            if(firstLine == true) {
                firstLine = false;
                if(!matches(line, PEM_CERT_PREFIXES)) {
                    throw new IllegalArgumentException("Invalid certificate format.");
                }
            } else {
                if(!scanner.hasNextLine()) {
                    if(!matches(line, PEM_CERT_POSTFIXES)) {
                        throw new IllegalArgumentException("Invalid certificate format.");
                    }
                } else {
                    stringBuilder.append(line);
                }
            }
        }
        scanner.close();
        String base64EncodedCert = stringBuilder.toString();
        validateCertificate(Base64.decode(base64EncodedCert, Base64.DEFAULT));
        return base64EncodedCert;
    }

    @Override
    public String parse(byte[] cert) {
        if(cert==null || cert.length == 0) {
            throw new IllegalArgumentException("Invalid certificate format.");
        }
        for(String string : PEM_CERT_PREFIXES) {
            if(startsWith(cert, string.getBytes())) {
                return parse(new String(cert));
            }
        }
        if(cert.length<MIN_VALID_CERT_LENGTH || cert.length>MAX_VALID_CERT_LENGTH) {
            throw new IllegalArgumentException("Invalid certificate format.");
        }
        validateCertificate(cert);
        return Base64.encodeToString(cert, Base64.DEFAULT);
    }

    private void validateCertificate(byte[] cert) {
        InputStream in = new ByteArrayInputStream(cert);
        X509Certificate certificate;
        try {
            CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
            certificate = (X509Certificate) certificateFactory.generateCertificate(in);
            if(!certificate.getSubjectDN().equals(certificate.getIssuerDN())) {
                throw new IllegalArgumentException("Certificate is not self-signed");
            }
        } catch (CertificateException e) {
            throw new IllegalArgumentException("Invalid certificate format", e);
        }
    }

    private boolean matches(String line, String[] strings) {
        for(String string : strings) {
            if(line.equals(string)) {
                return true;
            }
        }
        return false;
    }

    private boolean startsWith(byte[] source, byte[] match) {
        return startsWith(source, 0, match);
    }

    private boolean startsWith(byte[] source, int offset, byte[] match) {
        if (match.length > (source.length - offset)) {
            return false;
        }

        for (int i = 0; i < match.length; i++) {
            if (source[offset + i] != match[i]) {
                return false;
            }
        }
        return true;
    }
}
