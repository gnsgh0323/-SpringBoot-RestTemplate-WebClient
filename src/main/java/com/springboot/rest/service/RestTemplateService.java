package com.springboot.rest.service;

import com.springboot.rest.dto.MemberDto;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

@Service
public class RestTemplateService {

    public String getName() {
        // 1. HTTP 요청을 보낼 대상 URI를 생성합니다.
        URI uri = UriComponentsBuilder
                .fromUriString("http://localhost:9090")  // 기본 호스트와 포트를 지정한 기본 URI
                .path("/api/v1/crud-api")                // 요청 경로를 추가합니다
                .encode()                                 // URI를 인코딩합니다 (예: 공백을 %20으로 인코딩)
                .build()                                  // URI를 빌드합니다
                .toUri();                                 // URI 객체로 변환합니다

        // 2. RestTemplate 을 생성하고 설정합니다.
        //    주석 처리된 코드는 이전 질문에서 설명한 RestTemplate 빈을 사용하는 대안 코드입니다.
        //RestTemplate restTemplate = new RestTemplate();
        RestTemplate restTemplate = restTemplate(); // 이전에 구성한 RestTemplate 빈을 가져옵니다.

        // 3. 생성한 RestTemplate 을 사용하여 GET 요청을 보내고, 응답을 ResponseEntity 로 받습니다.
        ResponseEntity<String> responseEntity = restTemplate.getForEntity(uri, String.class);
        //   - GET 요청은 지정한 URI 로 보내며, 응답은 String 타입으로 예상합니다.

        // 4. 응답의 본문을 문자열로 추출하여 반환합니다.
        //    responseEntity.getBody()는 HTTP 응답의 본문을 가져옵니다.
        return responseEntity.getBody();
    }

    public String getNameWithPathVariable() {
        URI uri = UriComponentsBuilder
                .fromUriString("http://localhost:9090")
                .path("/api/v1/crud-api/{name}")
                .encode()
                .build()
                .expand("Flature")
                .toUri();

        //RestTemplate restTemplate = new RestTemplate();
        RestTemplate restTemplate = restTemplate();
        ResponseEntity<String> responseEntity = restTemplate.getForEntity(uri, String.class);

        return responseEntity.getBody();

    }

    public String getNameWithParameter() {
        URI uri = UriComponentsBuilder
                .fromUriString("http://localhost:9090")
                .path("/api/v1/crud-api/param")
                .queryParam("name", "Flature")
                .encode()
                .build()
                .toUri();

        //RestTemplate restTemplate = new RestTemplate();
        RestTemplate restTemplate = restTemplate();
        ResponseEntity<String> responseEntity = restTemplate.getForEntity(uri, String.class);

        return responseEntity.getBody();
    }

    public ResponseEntity<MemberDto> postWithParamAndBody() {
        URI uri = UriComponentsBuilder
                .fromUriString("http://localhost:9090")
                .path("/api/v1/crud-api")
                .queryParam("name", "Flature")
                .queryParam("email", "flature@wikibooks.co.kr")
                .queryParam("organization", "Wikibooks")
                .encode()
                .build()
                .toUri();

        MemberDto memberDto = new MemberDto();
        memberDto.setName("flature!!");
        memberDto.setEmail("flature@gmail.com");
        memberDto.setOrganization("Around Hub Studio");

        //RestTemplate restTemplate = new RestTemplate();
        RestTemplate restTemplate = restTemplate();
        ResponseEntity<MemberDto> responseEntity = restTemplate.postForEntity(uri, memberDto, MemberDto.class);

        return responseEntity;

    }

    public ResponseEntity<MemberDto> postWithHeader() {
        URI uri = UriComponentsBuilder
                .fromUriString("http://localhost:9090")
                .path("/api/v1/crud-api/add-header")
                .encode()
                .build()
                .toUri();

        MemberDto memberDto = new MemberDto();
        memberDto.setName("flature");
        memberDto.setEmail("flature@wikibooks.co.kr");
        memberDto.setOrganization("Around Hub Studio");

        RequestEntity<MemberDto> requestEntity = RequestEntity
                .post(uri)
                .header("my-header", "Wikibooks API")
                .body(memberDto);

        //RestTemplate restTemplate = new RestTemplate();
        RestTemplate restTemplate = restTemplate();
        ResponseEntity<MemberDto> responseEntity = restTemplate.exchange(requestEntity, MemberDto.class);

        return responseEntity;
    }

    public RestTemplate restTemplate() {

        // 1. HttpComponentsClientHttpRequestFactory 를 생성합니다.
        // RestTemplate 가 HTTP 요청을 만들 때 사용할 요청 팩토리
        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();

        // 2. HttpClient 를 생성하고 설정합니다.
        HttpClient client = HttpClientBuilder.create()    // HttpClient 생성
                .setMaxConnTotal(500)         // 전체 최대 연결 수 설정  -> 애플리케이션 전체에서 공유되는 최대 연결 수
                .setMaxConnPerRoute(500)      // 단일 라우트 당 최대 연결 수 설정  -> 하나의 호스트(서버)에 대한 동시 연결 수를 제한하는 데 사용
                .build();

        // 3. CloseableHttpClient 를 생성하고 설정합니다.
        // 생성하고 동일한 최대 연결 수를 설정합니다. 이것은 HTTP 클라이언트를 닫을 때 자원을 해제하기 위해 사용됩니다.
        CloseableHttpClient httpClient = HttpClients.custom()
                .setMaxConnTotal(500)         // 전체 최대 연결 수 설정
                .setMaxConnPerRoute(500)      // 단일 라우트 당 최대 연결 수 설정
                .build();

        // 4. HttpComponentsClientHttpRequestFactory 에 HttpClient 를 설정합니다.
        factory.setHttpClient(httpClient);

        // 5. 연결 제한 시간 (2초) 및 읽기 제한 시간 (5초)을 설정합니다.
        factory.setConnectTimeout(2000);     // 서버에 연결할 때까지 대기하는 시간
        factory.setReadTimeout(5000);        // 응답 데이터를 읽을 때까지 대기하는 시간

        // 6. 설정이 적용된 factory 를 사용하여 RestTemplate 을 생성합니다.
        RestTemplate restTemplate = new RestTemplate(factory);

        // 7. 생성된 RestTemplate 을 반환합니다.
        // RestTemplate 인스턴스를 반환합니다. 이 인스턴스는 설정된 연결 제한 시간과 최대 연결 수와 함께 HTTP 요청을 수행하는 데 사용됩니다.
        return restTemplate;
    }
}
