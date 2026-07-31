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

`spring.sql.init.mode=never` のため、起動時に `schema.sql` / `data.sql` は**自動実行されない**。
テーブルの新規作成やカラム追加が必要な場合は、`C:\work\20260401_花キューピット\07_テーブル作成sql\` の各 SQL ファイルを psql または pgAdmin で手動実行すること。

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

---

## CSV ファイルインポート共通規約

本プロジェクトで CSV ファイルをインプットとして処理する機能すべてに適用する。

### 対応文字コード

| 文字コード | 判定方法 | 備考 |
|---|---|---|
| **UTF-8（BOM付き）** | 先頭バイト `EF BB BF` | Excel の「CSV UTF-8（コンマ区切り）」形式 |
| **Shift-JIS（MS932）** | BOM なし | 各決済会社からの受領ファイルに多い |

上記以外（UTF-8 BOMなし・UTF-16 等）は**エラーとして処理を中断する**。

### ヘッダー行の列名チェック

CSV を読み込む全機能で、**ヘッダー行の列名は検証対象としない**（列数のみを検証
対象とする）。列名の表記はファイルの作成元・作成時期によって変わり得るため、
列名の完全一致を要求すると、構造（列の並び・列数）は正しいCSVが列名のわずかな
表記ゆれだけで弾かれてしまうため。

- 対象：取引コード紐付データ作成（`PaygateMappingCsvValidator`）、JFTD精算データ
  作成のJCB（`JcbCsvFormatValidator`）・スマレジ（`SumarejoCsvFormatValidator`）、
  および `jftd_settlement.html` のフロントエンドJS（`checkShiftJisCsv`）。
  新しくCSV取込み機能・フォーマットバリデータを追加する場合も、ヘッダー列名の
  完全一致チェックは行わないこと。
- 各バリデータが持つ `EXPECTED_HEADERS`（期待される列名の配列）は、削除せず
  **エラーメッセージの列名ラベルとして**（例：「売上件数」の数値変換エラー等）
  引き続き使用する。配列自体をなくすとエラーメッセージが「列9」のような
  分かりにくい表記になってしまうため。
- 例外的に、`checkShiftJisCsv`（JCB・スマレジのフロントエンドチェック）は
  `EXPECTED_HEADERS` との不一致件数を**ユーザーには表示せず内部的にのみ**
  計算し、「UTF-8 BOMなしファイルの誤検出防止」（後述のフロントエンド実装規則
  3.）にのみ使用する。この用途はヘッダー名の妥当性チェックではなく文字コード
  判定の補助なので、削除しないこと。

### バックエンド実装規則

- CSV を読み込む全インポータークラスは `AbstractFileImporter#detectCharset(MultipartFile)` を
  使って文字コードを自動判定すること。ハードコードで `MS932` や `UTF-8` を指定しない。
- `detectCharset` の判定ロジック：
  1. `EF BB BF` → `StandardCharsets.UTF_8` を返す
  2. `FF FE` / `FE FF`（UTF-16）→ `IllegalArgumentException` をスロー
  3. それ以外 → `Charset.forName("MS932")` を返す
- UTF-8 BOM のファイルはヘッダー行の先頭に BOM 文字（U+FEFF）が付く。ヘッダー行は
  スキップするため影響はないが、スキップしない場合は `replace(/^﻿/, '')` で除去する。

### 多列固定フォーマットCSVの列位置管理

各決済会社所定申込フォーム作成のINPUT（230列）のように、**列数が多く今後も列の
追加・変更が想定される固定フォーマットCSV**をパースする場合、列位置は
`fields.get(70)`のような数値直書きにせず、**宣言順で自動採番されるenumの
`ordinal()`**を使うこと。

```java
private enum Column {
    RECORD_NUMBER,
    READER_SERIAL_NO,
    TERMINAL_ID,
    TRADE_CODE,
    // ... 以下、CSVの列順そのままに宣言する
}

record.setTradeCode(trim(fields.get(Column.TRADE_CODE.ordinal())));
```

- **理由**：数値直書きだと、CSVの途中に1列挿入・削除するたびに後続の全列番号を
  手作業でずらす必要があり、抜け漏れによる列ズレ事故が起きやすい。enumの
  `ordinal()`なら、該当する位置にenum定数を1行挿入・削除するだけで後続の番号が
  自動的にずれるため、手作業でのインデックス再計算が一切不要になる。
- **列名ベースのマッピングは使わないこと**。「ヘッダー行の列名チェック」の規約
  （本ファイル内）で述べている通り、ヘッダー行の列名はファイルの作成元・作成時期
  によって表記が変わり得るため検証対象にしない方針であり、列名をキーにした動的
  マッピングはこの前提と相性が悪い（列名が実データと食い違うと解決できず、
  かえって壊れやすくなる）。列**数**のみに依存する現行方針を維持しつつ、
  列**位置**の管理だけをenumで安全にする。
- `EXPECTED_COLUMN_COUNT`は`Column.values().length`から自動算出し、列を
  追加・削除しても手動更新が不要になるようにする。
- 対象：`ApplicationFormCsvParser`・`ApplicationFormCsvValidator`
  （230列、`IDX_TRADE_CODE`等の個別定数もenumの`ordinal()`から取得するよう
  統一する）。同種の多列固定フォーマットCSVを今後追加する場合も、この方式を使う。
- INPUTモデルクラス（`ApplicationFormInput`等のPOJO）・フィールドアクセサー
  （文字列キーのMapで実装済み）・出力Excel側の列マッピングは、それぞれ
  宣言順に依存しない、または実際の申込書テンプレートの固定レイアウトに対応する
  ものであるため、この対応は不要（列挿入の影響を受けるのはパーサー側の
  位置ベースアクセスのみ）。

### フロントエンド実装規則

ファイル選択時のフロントエンドチェック（`checkShiftJisCsv`）でも BOM を検出し、
サーバー送信前にユーザーへ案内する。

1. `FileReader.readAsArrayBuffer` でバイト列を取得する。
2. 先頭バイトで判定する：
   - `0xEF 0xBB 0xBF` → UTF-8 BOM として `TextDecoder('utf-8')` でデコードする。
   - `0xFF 0xFE` / `0xFE 0xFF` → 非対応エラーを即表示する。
   - それ以外 → まず `isUtf8WithoutBom(bytes)` で **UTF-8（BOMなし）判定**を行い、
     該当すれば「UTF-8（BOMなし）」エラーを表示する。該当しなければ
     `TextDecoder('shift-jis')` でデコードする。
3. `isUtf8WithoutBom` は「非ASCIIバイトを含み、かつ全体が
   `new TextDecoder('utf-8', { fatal: true })` で例外なくデコードできる（＝厳密
   UTF-8として妥当）」ときに真を返す**決定的判定**とする。Shift-JIS(MS932) の
   日本語バイト列はほぼ厳密UTF-8にならないため、これで BOMなし UTF-8 を確実に
   判別できる。**ヘッダー名・列数・選択中の決済種類（期待ヘッダー）に依存しない**
   ことが重要。旧方式（Shift-JIS デコード後のヘッダー不一致件数を UTF-8 デコード
   時と相対比較する方法）は、ファイルの種類と選択した決済種類が食い違うと
   両者の不一致件数が並んで検出できず BOMなしを取りこぼす不具合があったため、
   決定的判定へ置き換えた。旧方式のヘッダー不一致比較はフォールバックとして
   残してあるが、判定の主役は `isUtf8WithoutBom` とする。

### エラー時のユーザー案内

文字コードエラーが発生した画面には以下を注意文言として表示する。

- 対応文字コードは **UTF-8（BOM付き）** または **Shift-JIS（MS932）** である旨。
- エラー時の対処方法：Excel でファイルを開き「名前を付けて保存」→
  「CSV UTF-8（コンマ区切り）(*.csv)」を選択して保存し直す。

### インポート時のエラー行の扱い（部分登録）

「取引コード紐付データ作成」（`/paygate_mapping_create`）・「JFTD精算データ作成」
（`/jftd_settlement`）のアップロード（登録）処理は、**1件でもデータエラーが
あるとファイル全体を登録しない、という全件ロールバック方式は採らない**。

- エラーが発生した行（列数不足・数値/日付変換エラー・
  `m_paygate_store_mapping` にマッピングが存在しない行・CSV内取引コード重複等）は
  **その行だけを登録せずスキップ**し、ファイルの最後まで処理を継続する。
- エラーが無かった行は通常どおりすべて登録する（部分登録を許容する）。
- 発生した**全エラー**を `ImportResponse.errors` に含めて画面に返す。エラー件数の
  上限（旧 `MAX_IMPORT_ERRORS`）は設けない。フロントエンドのエラー一覧テーブルも
  全件をそのまま表示する（`errorLimitReached` は常に `false` になる）。
  `CsvFormatValidator.validate()` が返す `CsvValidationResult` 側にも旧
  `MAX_ERRORS=50` による打ち切りが残っていたが、これも同じ方針で撤廃済み
  （`CsvValidationResult.isErrorLimitReached()` は常に `false` を返す）。
  エラー件数を再度制限したくなった場合でも、`ImportResult` と
  `CsvValidationResult` の両方に手を入れないと片方だけ上限なし・もう片方だけ
  上限ありという不整合が起きるため注意すること。
- レスポンスの `success` は「エラーが1件も無かったか」を表す（`errors.isEmpty()`）。
  エラーが1件でもあれば `success=false` を返すが、`importedCount` には実際に
  登録できた件数が入る（0件とは限らない）。`errorMessage` に登録件数・エラー件数・
  データ行数を含めるため、画面側の実装（`success===false` のとき
  `errorMessage`・エラーテーブルを表示する分岐）はそのまま利用でき、
  部分登録の件数もメッセージ内で表示される。
- 対象クラス：`AbstractFileImporter`（`throwIfErrors`／`MAX_IMPORT_ERRORS` は廃止）、
  `JcbFileImporter`・`SumarejoFileImporter`・`NetstarFileImporter`・
  `RakutenpayFileImporter`・`JushinSbiFileImporter`・`PaygateMappingFileImporter`。
  各 `importFile()` は登録件数と全エラーをまとめた `ImportResult` を返す
  （`FileImporter` インタフェースの戻り値型）。`JftdSettlementService`／
  `PaygateMappingService` はこれを `ImportResponse` に変換して返す。
- 住信SBIは区分1（店舗ヘッダー）が1ファイル中に複数回登場するため、区分1の
  マッピング解決・データ検証に失敗した場合は、その区分1配下の区分2（明細）も
  取引コードが未解決である旨のエラーとしてスキップする（区分1の失敗を後続の
  区分2まで伝播させる。他の区分1ブロックの処理には影響しない）。
- `m_import_batch` は **`trade_code` カラムを持たない**（廃止済み）。JFTD精算
  データ作成は1ファイルに複数店舗の行が混在するためバッチ単位で取引コードを
  一意に決められず、取引コード紐付データ作成側もログインユーザーIDを設定して
  いただけで実運用上の利用価値が薄かったため、カラムごと削除した。取引コードは
  各明細テーブル（`m_jcb_sales_detail`等）側に行ごとに保持されているので、
  バッチ単位で参照したい場合はそちらを`batch_id`で突き合わせること。
- 代わりに `m_import_batch.error_count` で正常／エラーを判別する。
  `record_count`（成功件数）と対になるカラムで、`JftdSettlementService.
  importFile()`／`PaygateMappingService.importFile()`が
  `savedBatch.setErrorCount(result.getErrors().size())` を呼んで設定する。
  `error_count == 0` なら正常終了、`> 0` なら部分登録エラーがあったバッチと
  判別できる（バッチ自体は作成されるが一部の行だけ登録に失敗したケース。
  詳細は次項「アップロード時の事前フォーマットチェックと部分登録の関係」を参照）。

#### エラー訂正・同一ファイル再アップロードによる二重登録の防止（その他精算データ作成・JFTD精算データ作成）

部分登録方式により、1回目のアップロードでエラーがあっても、エラー行以外は
既にDBへ登録済みになる。この状態でINPUTを訂正し、全行入りの修正ファイルを
そのまま再アップロードすると、1回目で既に成功していた行が2回目でも重複登録され、
JFTD統合振込CSV作成の集計で二重カウントされる（実際に検討して見つかったリスク。
その他精算データ作成（stera code/stera JCB/steraクレジット）・JFTD精算データ作成の
5社分インポーター（Jcb/Sumarejo/Netstar/Rakutenpay/JushinSbi）の計8決済種類には、
PAYGATE店舗コードマッピングのような「取引コード単位の洗い替え」の仕組みが元々無い）。
加えて、**エラーが無く正常終了した同一ファイルをそのまま2回アップロードしても
検知できず、そのまま2重に登録されてしまう**不具合が別途見つかったため、この
ケースも合わせて検知する（後述）。

- **警告条件を「同じ決済種別の未確定データが存在する」ことにはしない**。複数ファイルを
  確定（CSV作成）前にまとめてアップロードする運用は正常系であり（`m_jftd_transfer_batch`
  確定時に未確定分をすべてまとめて集計する設計のため）、この条件だと通常運用でも毎回
  警告が出てしまう。
- 正しい条件は「同じ`payment_type`で未確定（`transfer_batch_id IS NULL`）かつ、
  次のいずれかに該当するバッチが既に存在する」場合のみ。
  - **`error_count > 0`**（訂正しての再アップロードを想定。「訂正しての再アップロード」
    でしか通常発生しない状態のため、正常な複数回アップロードと区別できる）
  - **アップロードされたファイルと内容（SHA-256ハッシュ、`m_import_batch.file_hash`）が
    完全一致する**（`error_count`に関わらず判定。同一ファイルの誤った再アップロードを
    想定）
  - `OtherSettlementService.findReplaceableUnprocessedBatch(paymentType, fileHash)`／
    `JftdSettlementService.findReplaceableUnprocessedBatch(paymentType, fileHash)`で判定する
    （旧`findErroredUnprocessedBatch`を拡張・改名）。
- 該当バッチが見つかり、かつ画面から`replace=true`が渡されていない場合は、何も登録せず
  `ImportResponse.replaceConfirmationRequired(ReplaceConfirmation)`を返す
  （`ReplaceConfirmation`は既存バッチのbatchId・fileName・recordCount・errorCountに加え、
  `lookupKeys`（既存バッチの`m_import_batch.lookup_keys`をカンマ区切りで分割した一覧）を持つ）。
  画面側（`other_settlement.html`／`jftd_settlement.html`）は`window.confirm()`でこれを
  ユーザーに提示し（識別キー一覧も表示し、どの取引の重複かを判断できるようにする）、
  同意されたら`replace=true`を付けて再送信する。
- `replace=true`の場合、`importer.deleteBatchData(existing.getBatchId())`で
  既存バッチの明細行を削除してから`importBatchRepository.delete(existing)`でバッチ自体も
  削除し、それから通常どおり新しいファイルをインポートする（この「削除して置き換え」動作は
  上記2条件のどちらで検知した場合も共通）。
- `FileImporter`インタフェースに`default void deleteBatchData(int batchId)`を追加済み
  （未実装なら`extractLookupKey`と同様`UnsupportedOperationException`）。8つのインポーター
  すべてでオーバーライドし、対応する明細テーブルの`deleteByBatchId(batchId)`
  （Spring Data JPAの導出削除クエリ、各リポジトリに追加済み）を呼ぶ。JushinSbiのみ
  `m_visa_master_store_header`・`m_visa_master_transaction`の2テーブルを削除する。
  新しいインポーター（決済種別）を追加する場合も、同様に`deleteBatchData`の
  オーバーライドと対応リポジトリへの`deleteByBatchId`追加を忘れないこと。
- `m_import_batch`には`file_hash`（VARCHAR(64)、ファイル全体のSHA-256）・
  `lookup_keys`（TEXT、識別キーのカンマ区切り）を追加済み（schema.sql・
  `07_テーブル作成sql\create_jftd_transfer_master_tables.sql`・
  `ImportBatch.java`の3点セット同期済み。ローカルDBにも`ALTER TABLE`適用済み）。
  `lookup_keys`は`FileImporter.extractAllLookupKeys(file)`（各インポーターが
  `extractLookupKey`と同じ列から全データ行の値を重複除去して収集する。
  8インポーター全てに実装済み）の戻り値を`String.join(",", ...)`で保存する。
- 新しい決済種類（インポーター）を追加する場合は、`extractLookupKey`だけでなく
  `extractAllLookupKeys`も必ず実装すること（デフォルト実装は
  `UnsupportedOperationException`をスローする）。忘れると、その決済種類だけ
  重複ファイル再アップロード時に確認ダイアログの識別キー一覧が空になる
  （ハッシュ一致自体は`file_hash`列で判定するため検知そのものは効くが、
  利用者への識別情報提示ができなくなる）。

#### 本機能の対象外の画面・今後CSV取込を実装する場合の注意

上記の重複登録防止は、**バッチ単位で行を積み増す方式**（`m_import_batch`の
`batch_id`ごとに明細行が追加され、既存データを消さない方式）を採る画面でのみ
必要になる。以下の4画面は方式が異なる、またはCSV取込自体が未実装のため、
現時点では対象外である。

- **取引コード紐付データ作成**（`/paygate_mapping_create`、`PaygateMappingFileImporter`）
  は対象外。理由：ファイル取込のたびに`paygateMappingRepository.deleteByTradeCodeIn(tradeCodes)`
  で該当取引コードの既存行を削除してから登録し直す「**取引コード単位の洗い替え**」方式
  （前述「ヘッダー行の扱い・重複チェック」参照）のため、同じファイルを何度アップロード
  しても最終的なデータは変わらず、蓄積型の重複は原理的に起こらない。
- **加盟会員店マスターデータ登録・更新**（`member_master.html`）・
  **各決済会社所定申込フォーム作成**（`application_form.html`）・
  **店舗・端末・加盟店番号データ作成**（`shop_data_create.html`）は現時点で
  静的モックアップであり、CSV取込機能自体が未実装。将来これらにCSV取込を実装する
  際は、**先に「バッチ積み増し方式」か「キー単位の洗い替え方式」かを決めること**。
  積み増し方式を採る場合は、本節の重複登録防止（`file_hash`・`lookup_keys`・
  `findReplaceableUnprocessedBatch`・確認ダイアログ）を最初から組み込むこと。
  洗い替え方式（取引コード紐付データ作成と同様、対象となる一意キー単位で
  削除してから登録し直す）を採る場合は不要。

#### アップロード時の事前フォーマットチェックと部分登録の関係（`CsvValidationResult.isFatal()`）

`PaygateMappingService.importFile()`／`JftdSettlementService.importFile()` は、
実際の登録処理（`XxxFileImporter.importFile()`）を呼び出す**前**に
`CsvFormatValidator.validate(file)` で事前チェックを行っている。この事前チェックの
結果でファイル全体を拒否するかどうかの判定には、**`CsvValidationResult.isValid()`
（`errors.isEmpty()`）を使ってはならず、必ず `isFatal()` を使うこと**。

- `isValid()` はエラーが1件でもあれば `false` になるため、これでゲートすると
  データ行1件の列数不足や取引コード重複だけでファイル全体が拒否され、
  `XxxFileImporter.importFile()` が持つ「該当行だけスキップして残りを登録する」
  部分登録処理に**到達できなくなる**（実際に発生した不具合。単体テスト仕様書の
  部分登録系テスト項目で、期待される `importedCount` が0件になってしまっていた）。
- `isFatal()` は、**部分登録では救済できない致命的エラー**（ファイル拡張子不正・
  空ファイル・ヘッダー行の列数不正）でのみ `true` になる。各 `CsvFormatValidator`
  実装（`PaygateMappingCsvValidator`・`JcbCsvFormatValidator`・
  `SumarejoCsvFormatValidator`・`NetstarsCsvFormatValidator`・
  `RakutenpayCsvFormatValidator`・`JushinSbiCsvFormatValidator`）は、これらの
  致命的エラーを追加する箇所（いずれも早期 `return` する箇所）で必ず
  `result.markFatal()` を呼ぶこと。
- データ行単位のエラー（列数不足・数値/日付変換エラー・マッピング未存在・
  取引コード重複等）は `markFatal()` を呼ばない。これらは `isFatal()==false` の
  まま `Service` 側の登録処理へ進み、`XxxFileImporter` 側で改めて該当行だけを
  スキップして処理を継続する（＝バリデータとインポーターで同種のチェックが
  二重に行われるが、ファイル全体を止めるか止めないかの判断が異なるため、
  この二重チェックは意図的な設計である）。
- 新しく `CsvFormatValidator` を追加・変更する場合は、致命的エラーの追加箇所
  すべてに `markFatal()` が付いているか必ず確認すること（付け忘れると
  `isFatal()` が常に `false` になり、逆に本来ブロックすべきヘッダー不正等まで
  そのまま登録処理に進んでしまう）。

#### エラーメッセージへの識別情報の埋め込み

行番号は `CsvValidationError.rowNumber`（画面のエラー一覧テーブルの「行番号」列）
で表示されるため、エラーメッセージ本文には**その行がどのデータか識別できる
キー**（取引コード・加盟店番号・端末識別番号・加盟店IDなど、決済種類ごとの
識別キー）を埋め込むこと。「列数が不足しています（7列）」のような識別情報の無い
メッセージは、複数店舗・複数行が1ファイルに混在するCSVでは「どの行のデータか」
が分からず調査しづらいため避ける。

- 書式例：`取引コード「37-04」: 列数が不正です。期待: 13列、実際: 7列`
- 列数不足などで本来読み出したい列自体が存在しない場合は、実際に読める範囲の
  列（例：JCBなら列数が2列以上あれば1列目の加盟店番号）を安全にフォールバック
  として使う。存在しない列インデックスへのアクセスは行わない。
- ファイル全体を拒否する致命的エラー（`isFatal()==true`）のサマリーメッセージ
  （`ImportResponse.errorMessage`）にも、行番号が意味を持つ場合（ヘッダー行の
  列数不正など）は「1行目: 」のように行番号を前置する
  （`PaygateMappingService.buildFatalDetailMessage()` /
  `JftdSettlementService.buildFatalDetailMessage()`）。

### ヘッダー行の扱い・重複チェック（取引コード紐付データ作成CSV）

「取引コード紐付データ作成」画面（`/paygate_mapping_create`）が取り込む PAYGATE
会員コード紐付 CSV は、**1行目の内容によらず常にヘッダー行として扱いスキップする**。
列名（`hana cupid管理番号`・`店舗名` 等）による一致チェックは行わない。

- 理由：列名の表記はファイルの作成元・作成時期によって変更される可能性があり、
  列名の完全一致を要求すると、構造（列の並び・列数）は正しいCSVが列名のわずかな
  表記ゆれだけで弾かれてしまうため。
- 検証するのは **拡張子（.csv）・列数（13列固定）・各データ行の取引コード
  （1列目）が空でないこと**に加え、下記の重複チェックのみとする。ヘッダー行の
  有無を自動判定する仕組みは持たない（＝ヘッダー行は必須。ヘッダーなし＝1行目
  からデータ、という運用はサポートしない）。
- **1取引コード（加盟店）に複数端末が存在する運用があるため、取引コード自体の
  CSV内重複は許容する**（旧仕様では取引コードの重複をエラーとしていたが、顧客
  提供の実データ（`会員コード紐付データ.xlsx`、4,128行）を検証した結果、3,219件
  中500件の取引コードが複数行（最大22行）を持つ正常なケースであることが判明した
  ため、方針を変更した）。
- 代わりに、各決済会社の精算データ取込み（JFTD精算データ作成）で取引コードを
  逆引きするキーとなる下記4項目は、**CSV内で重複していたらエラー**とし該当行を
  登録せずスキップする（重複を許すと`PaygateMappingRepository.findFirstByXxx()`
  がどちらか一方を不定に返し、精算データが誤った取引コードに紐付く恐れがある
  ため）。
  - 端末識別番号（`terminal_id`）
  - 加盟店番号(住信SBI)（`sbi_merchant_id`）
  - StarPay店舗コード(ネットスターズ)（`netstar_store_code`）
  - GW店舗コード(Rpay)（`rpay_store_code`）
- リーダーシリアル番号・加盟店番号(JCB)は重複チェックの**対象外**とする。
  - リーダーシリアル番号は実データ上、無関係な取引コード間でも重複しており
    識別キーとして信頼できないため（重複値302件のうち301件が別々の取引コード
    をまたいで出現）。
  - 加盟店番号(JCB)は1店舗が複数端末で同一のJCB契約（同一の加盟店番号）を
    共有するケースが実データ上に正常に存在するため（重複＝異常ではない。
    実データでは重複18件がすべて同一取引コード内での重複）。
- 対象クラス：`PaygateMappingCsvValidator`・`PaygateMappingFileImporter`。
  重複チェック対象の4項目・対象外の2項目は両クラスで必ず同期して修正すること。
- 同じCSVの取込は **取引コード単位の洗い替え**（存在しなければ新規登録、存在すれば
  該当取引コードのレコードのみ削除して登録し直す）とし、CSVに含まれない取引コードの
  既存データは削除しない（`m_paygate_store_mapping` 全体を無条件に削除する実装には
  しないこと）。1取引コードに複数端末の行がある場合は、その取引コードの全端末分の
  行がまとめて削除・再登録される。

### 取引コード（trade_code）の解決規則（JFTD精算データ作成）

「JFTD精算データ作成」画面（`/jftd_settlement`）が取り込む決済会社別の精算ファイル
（JCB・スマレジ・ネットスターズ・楽天ペイ・住信SBI）は、**1ファイルに花キューピット
全店舗分のデータが行単位（住信SBIは区分1ブロック単位）で混在する**。そのため
取引コードは、**ファイル単位で1回だけ解決してはならず、行（区分1ブロック）ごとに
その行自身の識別キーで `m_paygate_store_mapping` を引き直して解決する**。

- 背景：`m_paygate_store_mapping` は「1レコード＝1店舗」で、各決済会社の識別コード
  （JCB加盟店番号・端末識別番号・ネットスターズ店舗コード・住信SBI加盟店ID・
  Rpay店舗コード）と取引コード（hana cupid管理番号）を1対1で保持している。一方、
  決済会社から届く精算ファイルは店舗横断の集計ファイルであり、1ファイルに複数店舗の
  行が含まれる。ファイルの先頭1行だけで取引コードを解決して全行にコピーすると、
  マッピングが存在しない店舗の行にも別店舗の取引コードが誤って付与されてしまう
  （実際に発生した不具合）。
- 識別キーと `m_paygate_store_mapping` の対応カラム：

  | 決済種類 | 識別キーの取得元 | 対応カラム |
  |---|---|---|
  | JCB | 加盟店番号（列2） | `jcb_merchant_no` |
  | スマレジ（端末月額） | 端末識別番号（列6） | `terminal_id` |
  | ネットスターズ | 店舗コード（列B） | `netstar_store_code` |
  | 楽天ペイ | STORE_NO（列C） | `rpay_store_code` |
  | 住信SBI | 加盟店ID（区分1の6列目） | `sbi_merchant_id` |

- マッピングが見つからない行（住信SBIは区分1ブロック）は、その行の行番号・識別キー・
  列名を含むエラーとして収集し、**その行だけを登録せずスキップして処理を継続する**
  （エラー行の扱いの詳細は前項「インポート時のエラー行の扱い（部分登録）」を参照）。
- 住信SBIは区分1（店舗ヘッダー）が1ファイル中に複数回登場する。区分1ごとに解決した
  取引コードを、後続の区分2（明細）が次の区分1に達するまで引き継ぐ（1パス処理）。
  ただし「直前に区分1があった」という並び順だけを信用せず、区分2自身が持つ加盟店ID
  （[2]列）が直前の区分1の加盟店ID（[5]列）と一致するかも突き合わせる。一致しない
  区分2（ファイルの並び順が壊れている等）は誤った取引コードに紐付けないため
  エラーとしてスキップする（`JushinSbiFileImporter`の`currentMerchantId`）。
- 対象クラス：`JcbFileImporter`・`SumarejoFileImporter`・`NetstarFileImporter`・
  `RakutenpayFileImporter`・`JushinSbiFileImporter`。いずれも `PaygateMappingRepository`
  を注入し、`batch.getTradeCode()` を全行に使い回すのではなく、行ごとに
  `findFirstByXxx()` で引き直した値を明細エンティティに設定すること。
- `JftdSettlementService.importFile()` は `m_paygate_store_mapping` への照会を
  自分では行わない。`importer.extractLookupKey(file)` はデータ行が1件も無い
  構造的に空なファイルを早期に弾くためだけに呼び出し、戻り値（識別キー）は
  使用しない（トレードコードの解決・整合性保証は行ごとの処理に一本化する）。

### 口座マスタ（m_stera_store）の解決規則（その他精算データ作成）

「その他精算データ作成」画面（stera terminal、`SteraJcbFileImporter`・
`SteraCodeFileImporter`・`SteraCreditFileImporter`）が取り込む売上・精算明細は、
取引コード解決（`m_stera_terminal`との突合、上記と同様に行ごとに解決）に加えて、
**振込先口座マスタ（`m_stera_store`）との突合も必ずインポート時点で行う**。

- 突合のタイミングは**その他精算データ作成（インポート）時点**であり、統合振込CSV
  作成・帳票作成（キャッシュレス決済売上報告書等）の時点では行わない。CSV作成・
  帳票作成は、インポート時点で口座マスタまで解決済みのデータをそのまま整形して
  出力するだけの処理とする（突合ロジックを持たせない）。
- 解決した取引コードで `m_stera_store` にレコードが無い場合は、取引コード未解決の
  行と同じ扱い（エラーとして収集し、その行は保存せずスキップして処理を継続する。
  詳細は前項「インポート時のエラー行の扱い（部分登録）」を参照）とする。
- この規則により、明細テーブル（`m_stera_jcb_sales_detail`・
  `m_stera_code_settlement_detail`・`m_stera_credit_sales_detail`）に保存される
  行は常に振込先口座まで特定済みの状態になる。そのため統合振込CSV作成の確定処理は、
  JFTD側（`JftdTransferConfirmService`）と同じく `m_import_batch`（ファイル単位）の
  未処理分をまとめて処理済みマークすればよく、明細テーブル側に行単位の処理済み
  マーカー（`transfer_batch_id`等）を追加する必要はない。

---

## DB 設計規約

### テーブル命名規則

テーブル名にはカテゴリプレフィックスを付ける。プレフィックスは `m_` に統一する
（`m_employee`、`m_member_info`、`m_import_batch` のように、すべてのテーブルが
`m_` で始まる）。

- 新規テーブルを追加する際は必ず `m_` で始まるテーブル名とすること。
- プレフィックスなしのテーブル名（`employee`、`import_batch` など）は使用しない。

### 3ファイルの同期ルール

テーブルの追加・変更・削除を行う場合、以下の3箇所を**必ず同時に**修正する。
どれか1つだけ変更すると起動時にテーブル不一致でエラーになる。

| # | 対象 | 場所 |
|---|---|---|
| A | 実行 SQL | `src/main/resources/schema.sql` |
| B | 作業用 SQL（設計書） | `C:\work\20260401_花キューピット\07_テーブル作成sql\` |
| C | Java エンティティ | `src/main/java/com/cupit/model/XxxYyy.java` の `@Table(name = "m_xxx_yyy")` |

※ テーブル仕様書（`.xlsx`）は上記 B と内容を揃えること（手動更新）。

### 制約名・インデックス名の命名規則

| 種別 | パターン | 例 |
|---|---|---|
| PRIMARY KEY 制約名 | `pk_<テーブル名>` | `pk_m_import_batch` |
| インデックス名 | `idx_<テーブル略称>_<カラム略称>` | `idx_jcb_batch`、`idx_visa_hdr_merchant` |

- テーブル略称はテーブル名から代表的な単語を抜粋する（`m_jcb_sales_detail` → `jcb`）。
- 同一テーブルに複数インデックスがある場合はカラム略称で区別する（`_batch`、`_store`）。

### 共通カラムの規約

すべてのテーブルに以下の3カラムを設ける。

| カラム名 | 型・制約 | 用途 |
|---|---|---|
| `registered_date` | `DATE NOT NULL DEFAULT CURRENT_DATE` | レコード登録日 |
| `updated_date` | `DATE` | レコード更新日（更新時にアプリがセット） |
| `updated_by` | `VARCHAR(50)` | 最終更新者のユーザーID |

Java エンティティ側でも対応するフィールドを定義する。

```java
@Column(name = "registered_date", nullable = false)
private LocalDate registeredDate;

@Column(name = "updated_date")
private LocalDate updatedDate;

@Column(name = "updated_by")
private String updatedBy;
```

---

## 画面 UI 規約

### 表形式（一覧）画面

- 表形式のデータを表示する画面は**ページ指定 UI（ページネーション）**を必ず実装する。
  - 表示件数セレクト（10 / 20 / 30 / 50 / 100件）を設ける。
  - 現在位置サマリー（「○–○ / ○件」）を表の上部に表示する。
  - ページ番号ボタンは両端＋現在ページ周辺を表示し、間は「…」で省略する。
  - 前・次ナビボタンを設ける。
  - 実装パターンは `employee_list.html` に準拠する。

- 明細行は**できるだけ 1 行で収まる**ようレイアウトを調整する。
  - `table-layout: fixed` で列幅を明示的に指定する。
  - 各セルに `white-space: nowrap; overflow: hidden; text-overflow: ellipsis;` を設定し、
    はみ出した内容は省略記号（…）で示す。
  - 列幅はデータの実際の桁数・文字数に合わせて設定し、横スクロールで全列が見えるようにする。
  - テーブル領域に `overflow-x: auto` と `max-height` を設定し、ヘッダー行は `position: sticky` で固定する。
  - 日本語テキスト列（店名・ステータス等）はフォントを `var(--font-base)`、
    数値・コード列は `var(--font-mono)` を使い、`font-variant-numeric: tabular-nums` を設定する。

### データ更新前の確認ダイアログ

削除操作だけでなく、**データベースへの登録・更新を行う操作すべて**（新規登録・
更新の保存ボタン、CSVアップロードによる一括登録）で、実行前に `window.confirm()`
による確認ダイアログを表示する。削除確認（例：「この社員を削除しますか？」）と
同じ考え方を、登録・更新側にも一貫して適用する。

- **対象**：単票の登録・更新フォーム（`employee_edit.html`・
  `settlement_fee_rate_edit.html`・`settlement_item_code_edit.html`等）の保存
  ボタン押下時、および CSV アップロードでデータを登録する画面
  （`paygate_mapping_create.html`・`member_master.html`・
  `shop_data_create.html`・`jftd_settlement.html`・`other_settlement.html`等）の
  アップロードボタン押下時。
- 確認は**入力チェック（`validateForm()`等）に通った後**、実際にサーバーへ
  送信する直前に行う。入力エラーがある状態で確認ダイアログを出さない。
- メッセージは新規登録・更新でどちらの操作かが分かる文言にする
  （例：「この内容で〇〇を登録しますか？」／「この内容で〇〇を更新しますか？」）。
  アップロード系は「このファイルをアップロードし、データを登録しますか？」のように、
  ファイルアップロードであることが分かる文言にする。
- **対象外**：DB へ保存しない操作（`application_form.html` の Excel 生成
  `/application_form/generate` 等、生成・ダウンロードのみで登録を伴わない処理）。
- 新しい登録・更新フォームや CSV アップロード機能を追加する場合も、保存・
  アップロード実行前に確認ダイアログを表示することを忘れないこと。

