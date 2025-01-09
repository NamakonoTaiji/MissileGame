
## MissileSimulator概要
javaの勉強もかねてゲームを作りました。Copilotという補助輪を付けて制作したので完全に自分で考えて作ったわけではありません。<br>
旋回性能、空気抵抗などの条件が同じミサイルで様々な誘導アルゴリズムを試すことができます。<br>

## 使い方
メインクラスのあるMissileSimulator.javaをコンパイル/実行して、アプリケーションを起動します。

javaファイルのコンパイル
```sh
javac MissileSimulator.java
```
クラスファイルの実行
```sh
java MissileSimulator
```

## 環境
java 23.0.1 2024-10-15<br>
Java(TM) SE Runtime Environment (build 23.0.1+11-39)<br>
Java HotSpot(TM) 64-Bit Server VM (build 23.0.1+11-39, mixed mode, sharing)<br>

## 機能
- 様々な誘導アルゴリズムのシミュレート<br>
- プレイヤーの操作<br>
- サウンドエフェクト<br>

## 操作方法
- ↑キー長押しで加速<br>
- ←　→ キー長押しで旋回<br>
- SPACEキーで発射台からミサイル発射<br>
- zキーで対抗手段(フレア、チャフ)の放出<br>
- cキーで誘導アルゴリズムの変更<br>
- 1キーでズーム、2キーでズームアウト<br>

## ゲーム内仕様
### ミサイルについて
ミサイルは赤外線誘導とレーダー誘導に分けられ、4種類の誘導アルゴリズムを実装しています。※2025年1/08日時点<br>

### 赤外線誘導
視界に入る熱源を平均した位置へ向かいます。プレイヤーの後方排気や近距離の熱源に大きく誘引されます。<br>
赤外線を放出する対抗手段（フレア）により誘引することができます。<br>

### レーダー誘導 (Active Radar Homing)
- アクティブレーダー誘導<br>
レーダー波の反射に向かいます。ARHミサイルはミサイル自体が目標にレーダー波を照射して、その反射を追いかけます。<br>
一番大きいレーダー反射に向かい、距離が近いほどレーダー反射が大きくなります。(プレイヤーはチャフに対し1.1倍反射が大きいです)<br>
レーダー波を反射する対抗手段（チャフ）で誘引することができます。<br>

### 誘引アルゴリズム
- 単純追尾航法　PPN（Pure Pursuit Navigation）<br>
常に相手を向く形で飛んでいきます。<br>
- 比例航法 PN (Proportional Navigation) <br>
相手の移動方向に先回りするように飛んでいきます。<br>
LOS角という相手と自分を結ぶ線の角度の変化量に定数(たいていの場合3)をかけたものを自身の角速度として飛びます。<br>
- 修正比例航法 MPN（Modified Proportional Navigation）<br>
比例航法と単追尾のいいとこどりをした誘導アルゴリズムです。<br>
比例航法は先回りをする分、外側に膨らむので切り返されると曲がり切れずに通り過ぎてしまう場合があります。<br>
- 半自動指令照準一致線誘導 SACLOS（Semi-Automatic Command to Line of Sight）<br>
目標にレーザーを照射し、そのレーザーにミサイルが乗っかるように機動させる誘導方式です。<br>
光学照準という扱いにしたのでフレアやチャフで妨害できません。<br>

### サウンドエフェクトについて
ミサイル発射音、被弾音、ミサイル通過音、対抗手段使用音、音速突破音、レーダー照射警報音などを実装しました。<br>
捜索レーダー、追尾レーダー、発射探知に合わせて警報音が変わります。<br>

### 運動エネルギーの概念
運動方程式やエネルギーの概念を現実に忠実になるように落とし込む能力が無かったので簡略化して実装しました。<br>
適正旋回速度は800、高速域では舵が効きにくくなります。<br>
ミサイルやプレイヤーは旋回、空気抵抗に応じて減速します。<br>

### UIについて
左上に速度、発射台からのプレイヤーの角度、メモリ使用量、誘導アルゴリズム、速度、空気抵抗を表示しています。<br>
右下のボックスには被弾時にログが出ます。<br>
赤外線誘導ミサイルの視野角は半透明の赤色、レーダーの視野角は半透明の緑色で表示しています。<br>
フレアは黄色い点、チャフは灰色の点で描画しています。<br>
ブースター燃焼中は排煙が描画されます。<br>
