
package com.gw.mvc.routes;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.MappingBuilder;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.io.UnsupportedEncodingException;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.delete;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.put;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@AutoConfigureMockMvc(addFilters = false)
//Need 'strip-prefix' = 0 since we call the actual endpoint using mockMvc without the context-path(/gw-api).
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {"app.gateway.strip-prefix=0"})
class MvcGatewayComponentTests {

    private static final WireMockServer wireMockServer = createStartedMockServer();

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    protected MockMvc mockMvc;

    static WireMockServer createStartedMockServer() {
        var wireMockServer = new WireMockServer(options().dynamicPort());
        wireMockServer.start();
        return wireMockServer;
    }

    @DynamicPropertySource
    static void appProperties(DynamicPropertyRegistry registry) {
        //Need to set the target server for the gateway to be the wiremock server
        registry.add("app.gateway.remote-servers.target", wireMockServer::baseUrl);
    }

    @AfterAll
    static void after() {
        wireMockServer.stop();
    }

    @BeforeEach
    void init() {
        wireMockServer.resetAll();
    }

    @Test
    void whenTargetServerIsUpButUrlIsNotDefined_callArbitraryUrl_statusIs404() throws Exception {
        performCall(HttpMethod.GET, "/order/" + UUID.randomUUID(), null, status().isNotFound());
    }

    @ValueSource(strings = {"/basic/hello", "/custom/filter/java/hello", "/custom/filter/configuration/hello"})
    @ParameterizedTest
    void whenApiIsDefinedInMockServer_callBasicHello_statusIs200(String urlContext) throws Exception {

        String mockBody = "{\"hello\": \"world\"}";
        MappingBuilder mappingBuilder = getMappingBuilder(urlContext, HttpMethod.GET);
        wireMockServer.stubFor(mappingBuilder
                .willReturn(aResponse()
                        .withBody(mockBody)
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)));

        MockHttpServletResponse response = performCall(HttpMethod.GET, urlContext, null, status().isOk());

        verifyResponse(response, mockBody);
    }

    private void verifyResponse(MockHttpServletResponse response, String body) throws UnsupportedEncodingException {
        String contentAsString = response.getContentAsString();
        assertThat(contentAsString).isEqualTo(body);
    }

    private MappingBuilder getMappingBuilder(String url, HttpMethod httpMethod) {
        MappingBuilder mappingBuilder;
        switch (httpMethod.name()) {
            case "GET" -> mappingBuilder = get(urlEqualTo(url));
            case "PUT" -> mappingBuilder = put(urlEqualTo(url));
            case "POST" -> mappingBuilder = post(urlEqualTo(url));
            case "DELETE" -> mappingBuilder = delete(urlEqualTo(url));
            default -> throw new IllegalStateException("Unexpected value: " + httpMethod);
        }
        return mappingBuilder;
    }

    private MockHttpServletResponse performCall(HttpMethod method, String uri, Object req, ResultMatcher resultMatcher) throws Exception {
        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.request(method, uri)
                .contentType(MediaType.APPLICATION_JSON);

        if (req != null) {
            requestBuilder.content(objectMapper.writeValueAsString(req));
        }

        return mockMvc.perform(requestBuilder)
                .andExpect(resultMatcher)
                .andReturn()
                .getResponse();
    }


}
