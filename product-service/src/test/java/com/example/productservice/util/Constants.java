package com.example.productservice.util;

import com.example.productservice.model.Product;
import lombok.experimental.UtilityClass;

import java.util.Map;
import java.util.UUID;

@UtilityClass
public class Constants {

    public static final String CLIENT_ID = "test-client-id";
    public static final String CLIENT_SECRET = "dd34716876364a02d0195e2fb9ae2d1b";
    public static final String BASE_URI = "http://localhost:9999";
    public static final String TOKEN_ISSUER_PATH = "/auth/realms/e-com/protocol/openid-connect/token";
    public static final String GRANT_TYPE = "client_credentials";

    public static final String MOCK_TOKEN = "eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJlUzQwV3VWM2s5TDB2Zl" +
            "JTMTZtTDdkQ25HbWxSRnJxazhhTVRyX3dnOVB3In0.eyJleHAiOjE2ODczNzkzMjksImlhdCI6MTY4NzM3OTI2OSwianRpIjoiYjFi" +
            "Y2M4NmYtMzI1OS00NTllLTk1MTUtMDE0MTI3NjU1NGM5IiwiaXNzIjoiaHR0cDovL2xvY2FsaG9zdDo0OTc3MS9yZWFsbXMvbWFzdG" +
            "VyIiwic3ViIjoiNjNhYzg1YmUtNWM5OS00OWQ3LWJlMDQtY2RiNDQwMDQ4NTE5IiwidHlwIjoiQmVhcmVyIiwiYXpwIjoiYWRtaW4t" +
            "Y2xpIiwic2Vzc2lvbl9zdGF0ZSI6IjRiMmE5OTE5LTA0OTQtNDc1Ny05MmQ0LWQ0ODA5ZGEwOTExYSIsImFjciI6IjEiLCJzY29wZS" +
            "I6InByb2ZpbGUgZW1haWwiLCJzaWQiOiI0YjJhOTkxOS0wNDk0LTQ3NTctOTJkNC1kNDgwOWRhMDkxMWEiLCJlbWFpbF92ZXJpZmll" +
            "ZCI6ZmFsc2UsInByZWZlcnJlZF91c2VybmFtZSI6ImFkbWluIn0.o2EvhI8b0NgWXcXTIkphiKloRbLdxKsYOCov2KqKciWQkccptf" +
            "_q4BKlUObDKSR1A7vMG2HpGhAxEoeaA-ATT_sIEpS3MFctyCYpQF-lQRXaY4IT5Z7Mixdtr1wPm12imEfR0WhKyJ0b6n3J3ufcIxBo" +
            "BEHPwPS4RHVEq39GS0xTx6g3Hz8tx3l4iR3vqLw7vJTqxLz_xYE6QNawKmA5Puka6F1faTmBSR_BFgtwfl70oi7PiqxnrAiREoJq6Z" +
            "Dl4UAvKFq_pLMsXgBCkyuqzEkilxHWfGrne_Wd1spucYAZTVv0ntWaQTOBxhan4Lpn_hTZJGMJRNtex_Yyn8sBsg";

    public static final Product PRODUCT_1 = Product.builder()
            .id(UUID.fromString("a07102fc-dae4-4891-92d9-3ce7e5a44714"))
            .sellerId(UUID.fromString("10000000-0000-0000-0000-000000000000"))
            .name("Java Book")
            .description("For beginner java developers - all you need to know as a beginner in one book")
            .price(49.99)
            .build();

    public static final Product PRODUCT_2 = Product.builder()
            .id(UUID.fromString("1e2096bd-d92b-4df4-aeb2-b1443aae81b7"))
            .sellerId(UUID.fromString("10000000-0000-0000-0000-000000000000"))
            .name("JavaScript Book")
            .description("JavaScript basics for new developers")
            .price(60)
            .build();

    public static final Map<String, String> MOCK_BUYER = Map.of(
            "id", "20000000-0000-0000-0000-000000000000",
            "username", "buyer",
            "password", "pwd123"
    );

    public static final Map<String, String> MOCK_SELLER = Map.of(
            "id", "10000000-0000-0000-0000-000000000000",
            "username", "seller",
            "password", "pwd123"
    );
}
