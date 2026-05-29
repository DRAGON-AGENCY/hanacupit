Java言語でアプリケーションの開発を行う場合は、下記のコーディング規約に従う事。
1.名前には英単語を使用する。
 原則として英単語を使用する。 
 日本語をローマ字にしたものはやめること
2.大文字と小文字で名前を区別しない
 また紛らわしい命名も避けましょう
3.パッケージ名はすべて小文字にする
4.パッケージ名には省略語を使用しない
5.インポートでは*を省略しない
6.クラス名は役割を表す名前にする。
7.クラス名はPascal形式で記述する
8.クラス名はフルスペルで記述する。
  クラス名にはフルスペルの英単語を組み合わせて使うことで、そのクラスの役割を連想できるようにする。
  複数の単語でクラス名が長くなりすぎる場合は省略語を容認する。
9.抽象クラスの名前には「Abstract」をつける
10.インタフェース名はクラス名に準ずる
11.例外クラス名の末尾に「Exception」を付ける
12.テストクラスの末尾に「Test」を付ける
   テストクラスの名前はテスト対象のクラスの名前の末尾に「Test」を付けたものとします。
13.メソッド名は目的のわかる名前にする。
14.メソッド名はCamel形式で記述する
   メソッド名は一つ以上の英単語で命名
   最初の単語を小文字とし、続く単語の先頭文字を大文字にした小文字ベースの記述をする。
   また、各単語の前後にはアンダースコア「_」やハイフン「-」やドル記号「$」を使用しません
15.メソッドの役割の対称性を意識する。１
16.ゲッターメソッド名は「get+属性名」とする
17.セッターメソッド名は「set+属性名」とする
18.booleanを返すメソッドはtrue/falseの識別がわかる名前にする
19.変数には意味のある名前をつける
20.変数名はCamel形式で記述する
21.boolean型の変数はtrue/falseの識別がわかる名前にする
22.GUIコンポーネントの命名にはコンポーネントの型を付加する
23.引数名とフィールド名が同じになることを回避する。
24.定数名は「_」で区切った大文字表記とする。
25.1つのクラスは1つのソースファイルで定義する
26.推奨されないAPIを使用しない
   Javaのリファレンスマニュアルで「推奨されていません」と記載されているAPIは使用しない
27.使われないコードは書かない
   使われていないprivateメソッドやローカル変数は、プログラム中に放置せず、消去する
28.クラスやメソッドのアクセス修飾子の宣言は適切な権限で行う
　public,protected,private,およびアクセス修飾子なしの4種類の可視性を適切に使い分ける。
　　オブジェクトのカプセル化を実現
　public
　　その継承階層に属さない外部のオブジェクトから呼ばれる必要のあるメソッドはpublicにします
　protected
　　クラス階層の中で下位のクラスからの呼び出される場合
　　同一クラス内で親メソッドから呼び出される子メソッドがある場合に、子メソッドをprotectedにする。
　private
　　自クラスのメソッドからしか呼び出されない。
　フィールドで宣言された変数は原則としてprivateにする
　　下位クラスへのインターフェースとして必要なものをprotectedとします。
　原則としてpublicやアクセス修飾なしにはしない
　　定数フィールドで自クラス以外から参照されるものをpublicとする。
29.メソッド定義とメソッド定義の間に空行を入れる。
   メソッド定義とメソッド定義の間に空行を1行入れる
   コメントアウトされている場合その次に空行を入れる
30.プリミティブ型と参照型の違いを認識する
　メソッドの引数は入力パラメーター。引数に対して値の代入や属性の更新はしない
31.ラッパークラスよりプリミティブ型を使う
　ラッパークラス型(Integer,Double,Boolean)よりプリミティブ型(int,double,boolean)を使う
　　コレクション要素を使う場合はラッパークラス型を使う
　ラッパークラスはオブジェクト値としてnullになる
　　Null PointExcepionがスローされる可能性
「==」演算で同値比較ができません。
32.クラス定義の記述順序を守る
　ソースファイルの中に構成要素の記述順序に一貫性を持たせる可読性が高まる
　　ソースファイル開始コメント
　　package文
　　import文
　　クラス、インタフェースのドキュメンテーション
　　class,Interfaceの宣言
　　staticなフィールド（クラス変数）の宣言
　　static以外のフィールド（インスタンス変数）の宣言
　　コンストラクト定義
　　メソッド定義
33.110行を超える行は改行または文を分割する
　開発プロジェクトで指定した桁位置を超えるような長い行をコーディングしません。
　コードの右側「//」で開始する行末コメントは記述しない
　Javadoc用のコメント分は80桁いないに収まるようにする。
34.行の途中での改行は、カンマの後、演算子の前、節（予約語）の前とする
　行が長くてスクロールが必要になるときは、行の途中で改行して読みやすくする。
　クラスやメソッドの宣言行では、まずextends、implements、throw節の前で改行し、次にカンマの後ろで改行します。
　改行後の行の先頭は4桁分インデント（字下げ）します。
35.クラスとメソッドの宣言行およびブロック開始行の末尾に「{」を記述する
　クラスの宣言の行の末尾に「{」を記述します。
　if,for,while,do,switchなどの制御文なども同様に「{」記述
　「{」の前に半角の空行文字を1文字いれる。
36.「{」の後ろにステートメントを記述しない
　「{」は行の末尾にしか記述しないため、ステートメント（文）を記述しない
　　どんなに短い文であっても「{」で必ず改行してから事業から記述する
37.インデントは半角の空白文字を使い4桁分とする
　開発プロジェクトで特に取り決めがない限り、インデントは半角の空白文字で4桁ずつとします。
　　インデントにはタブ文字（TAB）やセミコロン「;」を使用しない
38.ブロックの開始行と終了行のインデントを揃える
　ブロック開始行の先頭と、ブロックの終了位置の「}」のインデントを揃えます。
　　ブロックごとに改行すると可読性が低下するため。
39.ブロックの内部のインデントを揃える
40.無駄な空行は入れず意味のある切れ目で入れる
41.1行に2つ以上のステートメントを書かない
42.比較演算43.オートボクシングを使用しない子は「<」か「<=」を使う
43.オートボクシングを使用しない
44.カンマ、セミコロン、コロンの後ろや演算子の前後に空白を入れる
45.継承させたくないクラスにはfinal宣言をする
46.クラスにtoStringメソッドを装備する
47.メソッドのないインタフェースを定義せず定数クラスを使う
48.定数クラスにはstaticイニシャライザを使う
49.メソッドの最大行数は150行とする
50.循環的複雑度の上限を19とする
51.オーバーライドさせたくないメソッドはfinal宣言をする
52.メソッドの引数の順序には根拠がある
53.配列やコレクションを返すメソッドではnullを返さない
54.引数の数は少な目にする
55.引数の正当性を検査する
　publicメソッドとコンストラクタの引数は、その正当性を検査してから内部の処理に引き継ぐ
　　インデックス値の場合、負でないことをチェック。あるいは上限、下限のチェック
　　オブジェクトの参照である場合は、nullでないことをチェック
　　複数のパラメータを保持したパラメータ専用クラスのインスタンスで、自己の検査メソッドを有する場合は、その検査メソッドを呼び出してチェックする。
　引数のチェックの結果、正当性が否定された場合は、例外をスローする。
　protectedメソッド、privateメソッドの場合は、呼び出し元を限定できるので、冗長の検査は行わない。
56.クラスメソッドを実行するときはクラス名を使って呼び出す
57.定数はstatic fainalを宣言する
58.リテラルは原則として使わず定数を使う
59.配列の宣言は型名に「[]」を付けて行う
60.配列は宣言時に大きさを明確にする
61.2次元以上の配列を宣言しない
62.配列のコピーにはarraycopyメソッドを使用する
63.インスタンス変数は必ず初期化する
64.インスタンス変数はprivateで宣言する
65.むやみにアクセッサ（セッター、ゲッター）を定義しない
66.クラス変数にpublic static final宣言した配列を利用しない
67.クラス変数はクラス名を使ってアクセスする
68.ローカル変数名とフィールド名が同じになることを回避する
69.ローカル変数は安易に再利用しない
70.ローカル変数は使用する直前に初めて宣言と初期化を行う
71.更新される文字列にはStringBuilderを使用する
72.更新されない文字列にはStringを使用する
73.プリミティブ型とStringオブジェクトとの返還には変換メソッドを使う
74.誤差のない計算をするにはBigDecimalを使用する
75.数値の制度に気を付ける
　有効桁数に気を付ける
　数値型の仕様一覧
　　|数値型|サイズ|値の範囲|有効桁数|
　　|:---|:---:|:---:|---:|
　　|short,Short|16ビット|-32768~+32767|4桁|
　　|int,Integer|32ビット|-2147483648~+2147483647|9桁|
　　|long,Long|64ビット|-9223372036854775808~+9223372036854775807|18桁|
　　|float,Float|32ビット|多すぎるため割愛|18桁|
　　|float,Float|32ビット|多すぎるため割愛|18桁|
76.4桁以上の数値リテラルには3桁ごとにアンダースコアを挿入する(JavaSE7以降)
77.低精度なプリミティブ型にキャストしない
　精度の高いプリミティブ型から精度の低いプリミティブ型にキャストしない
　丸めや情報落ちやオーバーフロー/アンダーフローが起こり、もともと持っていた情報を失う可能性
　例

　　long型→int型にキャストでオーバーフローが起きる可能性
　　long型やint型をfloatにキャストすると情報落ちが起きる可能性
　　double型やfloat型をintにキャストするとオーバーフロー、アンダーフロー、丸め誤差が起きる可能性
　　ただし、意図的な丸目を起こすために、double型やfloat型をintにキャストすることがある。
78.スーパークラスのインスタンス変数をサブクラスで重複して定義しない
79.スーパークラスのprivateメソッドと同名のメソッドをサブクラスで定義しない
80.equalsメソッドを実装した場合にはhashCodeメソッドも実装する
81.Cloneableインタフェースは明示的に実装
82.オブジェクト同士はequalsメソッドで比較する
　同じクラスの2つのインスタンスが保持している値の同値値を判定するにはequalsメソッドを使う
　オブジェクト同士は「==」では比較しない
　　「=」はプリミティブ型のみ
　オブジェクトとnullとの比較にはequalsは使わない
　　この場合「==」か「!=」を使用する
　Stringではequalsで比較をする
　equaをオーバーライドしている主なクラスは以下の通り
　　ArrayList, LinkedList, Vector
　　BigDecimal
　　Color
　　Date
　　Double
　　File
　　Float
　　HashMap, TreeMap, EnumMap
　　HashSet,TreeSet,EnumSet
　　Integer
　　Long
　　String
83.instanceofをキャスト可否判断に使う
84.制御文の「{}」は省略しない
85.for分では3つのカウンタ条件を完備させる
86.for文とwhile文を正しく使い分ける
　for文:繰り返しの終了判定にループを数えるカウンタ、またらイテレータの使用が当てはまるときに使います。終了タイミングは毎回ループの直前
　拡張for文:配列またはコレクションの全要素に処理を適用する場合に使います	終了判定のタイミングは毎回のループの直前
　while:for文に適さないカウンタ、またはカウンタ以外の終了条件でループの終了を判定する場合に使います。終了判定のタイミングは毎回のループの直前
　do-while文:カウンタまたはイテレータ以外でループの終了を判定する場合に使います。終了判定のタイミングは毎回のループの最後。そのため最低1回はループの中実行する
87.for文を利用した繰り返し処理の中でカウンタ変数の値を変更しない
88.配列やコレクションを処理するループに拡張for文を使う
89.繰り返し処理中のオブジェクトの生成は要否を考える
90.繰り返し処理のループの中に tyr/catch ブロックを記述しない
91.switch文ではcaseごとにbreakを書く
92.switch文ではdefaultを必ず書きbreakも書く
93.switch文で文字列による分岐の際に文字列をnullチェックする(JavaSE7以降)
94.CollectionやMapにはジェネリクスを使う
95.オブジェクトの集合の繰り返し処理にはStream APIを使用する(JavaSE8以降)
96.AutoCloseableを実装していないストリームを扱うときはfinallyブロックでクローズ処理をする
97.AutoCloseableを実装しているストリームを扱うときはリソース付きtry文を使用する(JavaSE7以降)
98.ObjectOutStreamではresetメソッドを使用する
99.ストリームの操作でバッファ入出力を使う
100.catch文でキャッチする例外は詳細な例外クラスでキャッチする
101.Exceptionクラスのオブジェクトを生成してスローしない
102.finallyブロックには戻り値に影響がある記述をしない
103.catchブロックでは必ず処理をする
104.Error,Throwableクラスを継承しない
105.finalizeはオーバーライドしない
106.アプリケーションからfinalizeを呼び出さない
107.コメントは説明したいコードの直前の行に記述する
108.コメントは必要なものだけを簡潔に書く
109.コメントアウトしたプログラムの断片を次工程まで放置しない
110.Javadocの記述を揃える
111.オーバーライドするメソッドにはOverrideアノテーションを使用する
112.関数型インタフェースの定義にはFunctionalInterfaceアノテーションを使用する(JavaSE8 以降)

---

## レスポンス（応答性能）を考慮したコーディング

画面・APIの応答速度を意識して実装する。「動く」だけでなく「待たせない」ことを重視する。

### フロントエンド（画面表示）

- 外部CDN・Webフォント等のレンダリングをブロックするリソースは、非同期で読み込む。
  本プロジェクトは社内ネットワークの外部HTTPSが遅い／遮断されることがあり、`<head>`
  内の同期 `<link rel="stylesheet">` が読み込み完了まで画面描画を止めるため、初期表示が
  数秒間ブランクになる。`media="print" onload="this.media='all'"` で非ブロッキング化し、
  JS無効環境向けに `<noscript>` のフォールバックを併記する。代替（システム）フォントで
  即座に描画し、外部リソースは読み込めたら差し替える。
- Webフォントの読み込みには `&display=swap` を付け、フォント取得待ちで文字が見えなくなる
  のを防ぐ。
- 初期表示に不要なデータ・スクリプトは遅延読み込みにする。
- 時間のかかる操作はボタンを無効化し、処理中であることをユーザーに示す。

### サーバーサイド（処理ロジック）

- 不要なDBアクセスを発行しない。値が変わらない場合はUPDATEを実行しない、同じ値を
  ループ内で何度も問い合わせない、など。
- N+1問題を避ける。一覧取得では必要な件数を1～数回のクエリで取得する。
- ループの内側でDB／ファイル等のI/Oを行わない（一括取得・一括更新にまとめる）。
- BCryptなど高コストな処理を重複して実行しない。
- 画面初期表示に不要な重い集計・取得は、必要になった時点で実行する（遅延取得）。
- SELECTは必要な列・件数に絞る。全件・全列の取得を安易に行わない。

---

## ランタイム構成・デプロイ手順

本プロジェクトは **外部 Tomcat 10.1 + PostgreSQL 17** で稼働する Spring Boot WAR アプリ。組み込み Tomcat ではなく、Windows サービスとして動作する Apache Tomcat 10.1 にデプロイする。

### スタック

| 項目 | 値 |
|---|---|
| Tomcat | `C:\Program Files\Apache Software Foundation\Tomcat 10.1` |
| Tomcat サービス名 | `Tomcat10` |
| HTTP ポート | 8080 |
| WAR 出力 | `C:\git\hanacupit\target\ROOT.war`（pom.xml の `<finalName>ROOT</finalName>`） |
| デプロイ先 | `C:\Program Files\Apache Software Foundation\Tomcat 10.1\webapps\ROOT.war` |
| 接続先 DB | **PostgreSQL 17（ポート 5432）** 2026-05-29 の再インストールで 5433→5432 に変更。PG9.5 は撤去済みで PG17 のみ |
| DB ロール / DB 名 | `hanacupit` / `hanacupit`（ローカル開発用パスワード `hanacupit`） |
| Java | JDK 21（pom.xml `java.version=21`）。サービス起動 JVM は jdk-23 でも動作確認済み |

### `application.properties` のプレースホルダ

```
spring.datasource.url=${DB_URL:jdbc:postgresql://localhost:5432/hanacupit}
spring.datasource.username=${DB_USERNAME:hanacupit}
spring.datasource.password=${DB_PASSWORD:hanacupit}
```

環境変数 `DB_URL` / `DB_USERNAME` / `DB_PASSWORD` で上書き可能。未設定時は上記デフォルトを使用する。

`spring.sql.init.mode=always` のため、起動のたびに `src/main/resources/schema.sql` `data.sql` が実行される（`CREATE TABLE IF NOT EXISTS` で冪等）。

### デプロイ手順

1. **WAR ビルド**：Eclipse でプロジェクト右クリック → `実行` → `Maven clean` → `Maven install`
2. **再デプロイ**（管理者 PowerShell）：
   ```powershell
   Stop-Service Tomcat10
   Remove-Item -Recurse -Force 'C:\Program Files\Apache Software Foundation\Tomcat 10.1\webapps\ROOT'
   Remove-Item -Force 'C:\Program Files\Apache Software Foundation\Tomcat 10.1\webapps\ROOT.war'
   Copy-Item 'C:\git\hanacupit\target\ROOT.war' 'C:\Program Files\Apache Software Foundation\Tomcat 10.1\webapps\ROOT.war' -Force
   Start-Service Tomcat10
   ```
   ※ 展開済み `webapps/ROOT/` を消さずに上書きすると、古い設定で起動してしまうことがあるため必ず削除する。
3. 起動完了まで約 30〜40 秒待ってから `http://localhost:8080/` にアクセス。

### DB 初期構築（初回のみ）

PostgreSQL 17 の `postgres` スーパーユーザーで以下を実行:

```sql
CREATE ROLE hanacupit WITH LOGIN PASSWORD 'hanacupit';
CREATE DATABASE hanacupit OWNER hanacupit ENCODING 'UTF8';
GRANT ALL PRIVILEGES ON DATABASE hanacupit TO hanacupit;
```

### HTML テンプレートの配置

- Controller から `return "xxx";` でレンダリングする画面 → `src/main/resources/templates/xxx.html`
- 静的 HTML / CSS / JS / 画像 → `src/main/resources/static/`
- プロジェクト直下（`C:\git\hanacupit\*.html`）にあるファイルは **WAR に含まれない**（デザイン原本扱い）

### トラブルシューティング

- **404 が出るが画面が出ない** → ルート原因は WAR デプロイ失敗のことが多い。`C:\Program Files\Apache Software Foundation\Tomcat 10.1\logs\catalina.<日付>.log` を末尾から確認し、`HostConfig.deployWAR` の `重大` 行から `Caused by:` を辿る。
- **DB 接続エラー / 認証失敗** → 接続先は PG17（5432）。接続タイムアウト時は待受ポート不一致を疑う（`Get-NetTCPConnection -State Listen -LocalPort 5432` で確認）。
- ログのエンコーディングは Shift_JIS。PowerShell で読むと日本語が化けるが、クラス名・例外型は ASCII なので原因特定は可能。

---

## セキュリティ（CSRF 対策）

本プロジェクトは Spring Security のフィルタチェーンを使わず（依存は BCrypt 用の
`spring-security-crypto` のみ）、独自のセッション認証インターセプターで動作する。
そのため Spring 標準の CSRF 機構が無く、**シンクロナイザートークン方式を自前で
実装している**。状態を変更するリクエストには CSRF トークンの送信が必須。

### 構成要素

| 役割 | クラス／ファイル |
|---|---|
| トークン生成・保持 | `com.cupit.security.CsrfTokenManager` |
| トークン検査（更新系のみ） | `com.cupit.interceptor.CsrfProtectionInterceptor` |
| 全画面へトークン供給 | `com.cupit.advice.CsrfTokenControllerAdvice` |
| インターセプター登録 | `com.cupit.config.WebConfig` |

- `CsrfTokenManager`：`SecureRandom`(32 バイト)＋Base64URL でトークンを生成し、
  セッション属性 `csrfToken` に 1 セッション 1 トークンで保持する。
- `CsrfProtectionInterceptor`：更新系メソッド（POST / PUT / PATCH / DELETE）のみ
  検査する。リクエストヘッダー `X-CSRF-TOKEN` とセッションのトークンを定数時間比較
  （`MessageDigest.isEqual`）し、不一致・欠落は **403（Forbidden）** で中断する。
  参照系（GET など）は検査せず通過させる。
- `CsrfTokenControllerAdvice`：`@ControllerAdvice` ＋ `@ModelAttribute` で全画面の
  モデルにトークン（モデル属性名 `csrfToken`）を供給する。トークンが無ければ生成する。
- `WebConfig`：CSRF 検査は **`/login` の POST も対象**にするため、認証インターセプター
  のように `/` `/login` を除外せず、静的リソース・favicon・error のみ除外する。
- リダイレクト時にトークンが URL のクエリへ漏れないことは、`@EnableWebMvc` を使わず
  Spring Boot 自動設定（`ignoreDefaultModelOnRedirect=true`）に委ねることで担保している。

### 画面（テンプレート）側の実装

状態を変更するリクエスト（`fetch` の POST 等）を行う画面では、以下を必ず行う。

1. `<head>` にトークンを埋め込む。Thymeleaf でレンダリングされるため `th:content` で
   差し込む（生 HTML 表示時のフォールバックとして空の `content=""` も併記する）。
   ```html
   <meta name="csrf-token" th:content="${csrfToken}" content="">
   ```
   `th:` を使うため `<html lang="ja" xmlns:th="http://www.thymeleaf.org">` と宣言する。
2. JS で meta からトークンを読み取り、更新系 `fetch` のヘッダーに付与する。
   ```javascript
   var csrfToken = document
       .querySelector('meta[name="csrf-token"]').getAttribute('content');
   fetch('/path', {
       method: 'POST',
       headers: {
           'Content-Type': 'application/json',
           'X-CSRF-TOKEN': csrfToken
       },
       body: JSON.stringify(payload)
   });
   ```

現在の対象エンドポイントは `/login`・`/employee/save`・`/employee/delete`。
**新たに状態変更エンドポイントや画面を追加する場合は、上記の meta 埋め込みと
`X-CSRF-TOKEN` ヘッダー付与を必ず実装する**こと（漏れると 403 で動かない）。

