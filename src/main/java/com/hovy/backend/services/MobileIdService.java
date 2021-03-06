package com.hovy.backend.services;

import com.hovy.backend.db.entities.Shop;
import com.hovy.backend.utils.CertUtil;
import com.hovy.backend.utils.MobileIdSSL_IT;
import ee.sk.mid.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.cert.X509Certificate;

import static com.hovy.backend.utils.TestData.*;
import static java.util.Arrays.asList;

@Service
public class MobileIdService {

    private KeyStore keystoreWithDemoServerCertificate;

    private MidClient client;

    @Autowired
    private ShopService shopService;


    @PostConstruct
    public void init() {
        try {
            InputStream is = MobileIdSSL_IT.class.getResourceAsStream("/demo_server_trusted_ssl_certs.jks");
            keystoreWithDemoServerCertificate = KeyStore.getInstance("JKS");
            keystoreWithDemoServerCertificate.load(is, "changeit".toCharArray());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        client = MidClient.newBuilder()
                .withRelyingPartyUUID(DEMO_RELYING_PARTY_UUID)
                .withRelyingPartyName(DEMO_RELYING_PARTY_NAME)
                .withHostUrl(DEMO_HOST_URL)
                .withTrustStore(keystoreWithDemoServerCertificate)
                .build();
    }

    public String challenge(String phoneNumber, String personalIdNumber, Shop shop) {
        MidAuthenticationHashToSign authenticationHash = MidAuthenticationHashToSign.generateRandomHashOfDefaultType();
        String verificationCode = authenticationHash.calculateVerificationCode();

        System.out.println("Verification code is " + verificationCode);

        MidAuthentication authentication = CertUtil.createAndSendAuthentication(client, phoneNumber, personalIdNumber, authenticationHash);

        CertUtil.assertAuthenticationCreated(authentication, authenticationHash.getHashInBase64());

        // WE TRUST BOTH production MID certificates (persons have copied their production
        // certificate to DEMO and are now using it) and TEST certificates as well (test numbers)

        X509Certificate caCertificateProdEnv1 = CertUtil.fileToX509Certificate("/trusted_certificates/ESTEID-SK_2011.pem.crt");
        X509Certificate caCertificateProdEnv2 = CertUtil.fileToX509Certificate("/trusted_certificates/ESTEID-SK_2015.pem.crt");

        X509Certificate caCertificateTestEnv1 = CertUtil.fileToX509Certificate("/trusted_certificates/TEST_of_ESTEID-SK_2011.pem.crt");
        X509Certificate caCertificateTestEnv2 = CertUtil.fileToX509Certificate("/trusted_certificates/TEST_of_ESTEID-SK_2015.pem.crt");


        MidAuthenticationResponseValidator validator = new MidAuthenticationResponseValidator(
                asList(caCertificateProdEnv1, caCertificateProdEnv2, caCertificateTestEnv1, caCertificateTestEnv2));

        MidAuthenticationResult authenticationResult = validator.validate(authentication);

        CertUtil.assertAuthenticationResultValid(authenticationResult);

        MidAuthenticationIdentity authenticationIdentity = authenticationResult.getAuthenticationIdentity();

        return getServices(
                String.format("Welcome %s %s!", authenticationIdentity.getGivenName(), authenticationIdentity.getSurName()),
                shop
        );
    }

    public String getServices(String personName, Shop shop) {
        String [] services = shopService.getServices(shop.getId());

        String[] jsonServices = new String [services.length];

        for (int i = 0; i < services.length; i++) {
            jsonServices [i] = String.format("{\"index\": %d, \"name\": \"%s\"}", i, services[i]);
        }

        return String.format("{" +
                "\"welcome\":\"%s\"," +
                "\"shop\": {" +
                        "\"id\": %d," +
                        "\"name\": \"%s\"," +
                        "\"address\": \"%s\"," +
                        "\"logoUrl\": \"%s\"" +
                "}," +
                "\"services\":[%s]" +
            "}",
            personName,
            shop.getId(),
            shop.getName(),
            shop.getAddress(),
            shop.getLogoUrl(),
            String.join(",", jsonServices));
    }

    public String getServices(Shop shop) {
        String [] services = shopService.getServices(shop.getId());

        String[] jsonServices = new String [services.length];

        for (int i = 0; i < services.length; i++) {
            jsonServices [i] = String.format("{\"index\": %d, \"name\": \"%s\"}", i, services[i]);
        }

        return String.format("{" +
                    "\"shop\": {" +
                        "\"id\": %d," +
                        "\"name\": \"%s\"," +
                        "\"address\": \"%s\"," +
                        "\"logoUrl\": \"%s\"" +
                    "}," +
                    "\"services\":[%s]" +
                "}",
                shop.getId(),
                shop.getName(),
                shop.getAddress(),
                shop.getLogoUrl(),
                String.join(",", jsonServices));
    }
}
