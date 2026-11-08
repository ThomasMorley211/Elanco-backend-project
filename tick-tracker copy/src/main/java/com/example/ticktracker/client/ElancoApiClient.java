package com.example.ticktracker.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.List;

@Component
public class ElancoApiClient{
    private static final Logger log = LoggerFactory.getLogger(ElancoApiClient.class);

    private final RestClient restClient;
    private final int maxRetries;
    private final long rateLimitWaitSeconds;

    public ElancoApiClient(RestClient.Builder builder,
        @Value("${elanco.api.base-url}") String baseUrl,
        @Value("${elanco.api.max-retries}") int maxRetries,
        @Value("${elanco.api.rate-limit-wait-seconds}") long rateLimitWaitSeconds){
            this.restClient = builder.baseUrl(baseUrl).build();
            this.maxRetries=maxRetries;
            this.rateLimitWaitSeconds = rateLimitWaitSeconds;
        }
        public List<SightingDto> fetchAllSightings(){
            String lastProblem = "Unknown problem";
            for (int attempt = 1; attempt <= maxRetries; attempt++){
                long waitMillis = 0;
                try{
                    List<SightingDto> body = restClient.get()
                    .uri("/tick-sightings")
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<SightingDto>>() { });
                    if (body == null) {
                        throw new UpstreamApiException("The API returned an empty response");
                    }
                    log.info("Downloaded {} sightings from the Elanco API", body.size());
                    return body;
                }catch (HttpClientErrorException.TooManyRequests e){
                    lastProblem = "rate limited (HTTP 429)";
                    waitMillis = rateLimitWaitSeconds * 1000;
                }catch(HttpServerErrorException e){
                    lastProblem = "server error (HTTP " + e.getStatusCode().value() + ")";
                    waitMillis=2000L * attempt;
                }catch (ResourceAccessException e){
                    lastProblem = "could not reach the API (" + e.getMessage()+")";
                    waitMillis = 2000L *attempt;
                }catch (HttpClientErrorException e){
                    throw new UpstreamApiException(
                        "the API rejected the request (HTTP" + e.getStatusCode().value() + ")",e);
                }catch (RestClientException e){
                    throw new UpstreamApiException("The API returned data in an unexpected format", e);
                    }
                log.warn("Attempt{}/{} failed:  {}", attempt,maxRetries,lastProblem);
                if (attempt<maxRetries){
                    log.info("Waiting {} seconds before trying again", waitMillis/1000);
                    pause(waitMillis);
                }
            }
            throw new UpstreamApiException(
                "The Elanco API was unavailable after" + maxRetries + " attempts: "+ lastProblem);
        }
        private void pause(long millis){
            try {
                Thread.sleep(millis);
            } catch(InterruptedException e){
                Thread.currentThread().interrupt();
                throw new UpstreamApiException("Interrupted while waiting to retry", e);
            }
        }
}
