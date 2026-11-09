# Fridge to Plate — data layer

UI에서는 `RepositoryProvider.recipes` 또는 주입받은 `RecipeRepository`를 사용합니다.
ViewModel의 coroutine에서 suspend 함수를 호출하고 `RepositoryResult.Success` /
`RepositoryResult.Failure`를 화면 상태로 변환하세요. coroutine 취소는 네트워크 요청도 취소합니다.

## API 키

사용자 홈의 `~/.gradle/gradle.properties`에 다음 속성을 추가하거나 같은 이름의 환경 변수를 설정하세요.

```properties
SPOONACULAR_API_KEY=your_key
```

프로젝트 Gradle 속성이 환경 변수보다 우선합니다. 키 변경 후 앱을 다시 빌드하세요.
키가 없어도 빌드되며 호출 시 `MissingApiKey`를 반환합니다.
개발용 BuildConfig 키는 APK에서 추출할 수 있습니다. 배포 시에는 서버 프록시를 통해
Spoonacular 키를 서버에 보관하는 구성을 사용하세요. API 키와 실제 사용자 데이터는 테스트에 필요하지 않습니다.

## UI 연결 예시

```kotlin
val result = repository.recommend(
    RecommendationRequest(
        ingredients = listOf("egg", "rice", "tomato"),
        ranking = RecommendationRanking.MINIMIZE_MISSING_INGREDIENTS,
    )
)
when (result) {
    is RepositoryResult.Success -> result.value // 추천 목록
    is RepositoryResult.Failure -> result.error // UI에서 처리
}
val detail = repository.getRecipe(id = 7)
```

- 입력: 영문 재료명 목록. 공백과 중복은 정리하며 빈 목록, 쉼표가 든 재료명, 1~100 외의 limit은 거부합니다.
- 추천: 사용한 재료 / 부족한 재료 / 사용하지 않은 재료와 개수를 제공합니다.
- 상세: 재료 수량, 인분, 조리 시간, 출처 URL, 섹션별 조리 단계, 식이 정보를 제공합니다.
- 상세 조리 단계가 비어 있으면 HTML일 수 있는 `instructionsHtml`을 대체 정보로 사용할 수 있습니다.
- 오류: 입력, 키 누락, 인증, 할당량, 호출 제한(Retry-After), 미발견, 네트워크, 응답 파싱, 기타 HTTP 상태.
- 추천 순서는 API 응답 순서를 유지합니다. 자동 재시도나 상세 정보 일괄 조회로 추가 할당량을 소비하지 않습니다.
- 한국어 번역, 냉장고 재료 영구 저장, 주간 식단 편성은 아직 구현하지 않았습니다.
- 식이 정보는 API 데이터이며 알레르기 안전성을 보장하지 않습니다.

공식 API 문서: https://spoonacular.com/food-api/docs

## 검증

`./gradlew :app:testDebugUnitTest :app:assembleDebug`

MockWebServer 테스트가 실제 API 호출 없이 요청 파라미터, 응답 매핑, 입력 검증, HTTP 오류를 검증합니다.
