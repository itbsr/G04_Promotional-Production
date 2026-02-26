alter session set container = dbsys;
CONNECT g2404/pass@localhost/dbsys

-- 初期データの挿入
insert into table_versions (
   table_name,
   version_number,
   modified_at
) values ( 'menus',
           0,
           systimestamp );
insert into table_versions (
   table_name,
   version_number,
   modified_at
) values ( 'options',
           0,
           systimestamp );
insert into table_versions (
   table_name,
   version_number,
   modified_at
) values ( 'categories',
           0,
           systimestamp );

-- ISNERT PARENTS
insert into categories (
   parent_category_id,
   category_name
) values ( null,
           'バーガー' );
insert into categories (
   parent_category_id,
   category_name
) values ( null,
           'サイドメニュー' );
insert into categories (
   parent_category_id,
   category_name
) values ( null,
           'ドリンク' );

-- ISNERT CHILDREN
insert into categories (
   parent_category_id,
   category_name
) values ( 1,
           'ハンバーガー' );
insert into categories (
   parent_category_id,
   category_name
) values ( 1,
           'サンドイッチ' );
insert into categories (
   parent_category_id,
   category_name
) values ( 2,
           'ポテト' );
insert into categories (
   parent_category_id,
   category_name
) values ( 2,
           'チキン' );
insert into categories (
   parent_category_id,
   category_name
) values ( 2,
           'サラダ' );
insert into categories (
   parent_category_id,
   category_name
) values ( 2,
           'その他' );
insert into categories (
   parent_category_id,
   category_name
) values ( 3,
           'ジュース' );
insert into categories (
   parent_category_id,
   category_name
) values ( 3,
           'コーヒー' );
insert into categories (
   parent_category_id,
   category_name
) values ( 3,
           'アルコール' );

insert into menus (
   menu_id,
   menu_name,
   category_id,
   menu_price,
   sort_order,
   menu_image
) values ( - 1,
           'ハンバーガー',
           4,
           170,
           10,
           'menuImages/hamburger.jpg' );
insert into menus (
   menu_id,
   menu_name,
   category_id,
   menu_price,
   sort_order,
   menu_image
) values ( - 2,
           'チーズバーガー',
           4,
           250,
           20,
           'menuImages/hamburger.jpg' );
insert into menus (
   menu_id,
   menu_name,
   category_id,
   menu_price,
   sort_order,
   menu_image
) values ( - 3,
           'ダブルチーズバーガー',
           4,
           450,
           30,
           'menuImages/hamburger.jpg' );
insert into menus (
   menu_id,
   menu_name,
   category_id,
   menu_price,
   sort_order,
   menu_image
) values ( - 4,
           'トリプルハンバーガー',
           4,
           600,
           40,
           'menuImages/hamburger.jpg' );
insert into menus (
   menu_id,
   menu_name,
   category_id,
   menu_price,
   sort_order,
   menu_image
) values ( - 5,
           'ベーコンチーズバーガー',
           4,
           550,
           50,
           'menuImages/hamburger.jpg' );
insert into menus (
   menu_id,
   menu_name,
   category_id,
   menu_price,
   sort_order,
   menu_image
) values ( - 6,
           'アボカドバーガー',
           4,
           550,
           60,
           'menuImages/hamburger.jpg' );
insert into menus (
   menu_id,
   menu_name,
   category_id,
   menu_price,
   sort_order,
   menu_image
) values ( - 7,
           'ベーコンレタスバーガー',
           4,
           550,
           70,
           'menuImages/hamburger.jpg' );
insert into menus (
   menu_id,
   menu_name,
   category_id,
   menu_price,
   sort_order,
   menu_image
) values ( - 8,
           'エッグバーガー',
           4,
           500,
           80,
           'menuImages/hamburger.jpg' );
insert into menus (
   menu_id,
   menu_name,
   category_id,
   menu_price,
   sort_order,
   menu_image
) values ( - 9,
           'エッグチーズバーガー',
           4,
           500,
           90,
           'menuImages/hamburger.jpg' );
insert into menus (
   menu_id,
   menu_name,
   category_id,
   menu_price,
   sort_order,
   menu_image
) values ( - 10,
           'テリヤキビーフバーガー',
           4,
           550,
           100,
           'menuImages/hamburger.jpg' );
insert into menus (
   menu_id,
   menu_name,
   category_id,
   menu_price,
   sort_order,
   menu_image
) values ( - 11,
           '4種のチーズバーガー',
           4,
           600,
           110,
           'menuImages/hamburger.jpg' );
insert into menus (
   menu_id,
   menu_name,
   category_id,
   menu_price,
   sort_order,
   menu_image
) values ( - 12,
           'クワトロチーズバーガー',
           4,
           600,
           120,
           'menuImages/hamburger.jpg' );
insert into menus (
   menu_id,
   menu_name,
   category_id,
   menu_price,
   sort_order,
   menu_image
) values ( - 13,
           'マッシュルームバーガー',
           4,
           600,
           130,
           'menuImages/hamburger.jpg' );
insert into menus (
   menu_id,
   menu_name,
   category_id,
   menu_price,
   sort_order,
   menu_image
) values ( - 14,
           'アボカドオニオンバーガー',
           4,
           500,
           140,
           'menuImages/hamburger.jpg' );
insert into menus (
   menu_id,
   menu_name,
   category_id,
   menu_price,
   sort_order,
   menu_image
) values ( - 15,
           'パインバーガー',
           4,
           550,
           150,
           'menuImages/hamburger.jpg' );
insert into menus (
   menu_id,
   menu_name,
   category_id,
   menu_price,
   sort_order,
   menu_image
) values ( - 16,
           'オニオンバーガー',
           4,
           500,
           160,
           'menuImages/hamburger.jpg' );
insert into menus (
   menu_id,
   menu_name,
   category_id,
   menu_price,
   sort_order,
   menu_image
) values ( - 17,
           'マッシュポテトバーガー',
           4,
           550,
           170,
           'menuImages/hamburger.jpg' );
insert into menus (
   menu_id,
   menu_name,
   category_id,
   menu_price,
   sort_order,
   menu_image
) values ( - 18,
           'チキンバーガー',
           5,
           250,
           180,
           'menuImages/hamburger.jpg' );
insert into menus (
   menu_id,
   menu_name,
   category_id,
   menu_price,
   sort_order,
   menu_image
) values ( - 19,
           'クリスピーチキンバーガー',
           5,
           420,
           190,
           'menuImages/hamburger.jpg' );
insert into menus (
   menu_id,
   menu_name,
   category_id,
   menu_price,
   sort_order,
   menu_image
) values ( - 20,
           'エビカツバーガー',
           5,
           450,
           200,
           'menuImages/hamburger.jpg' );
insert into menus (
   menu_id,
   menu_name,
   category_id,
   menu_price,
   sort_order,
   menu_image
) values ( - 21,
           'グリルチキンバーガー',
           5,
           450,
           210,
           'menuImages/hamburger.jpg' );
insert into menus (
   menu_id,
   menu_name,
   category_id,
   menu_price,
   sort_order,
   menu_image
) values ( - 22,
           'ソイミートバーガー',
           5,
           500,
           220,
           'menuImages/hamburger.jpg' );
insert into menus (
   menu_id,
   menu_name,
   category_id,
   menu_price,
   sort_order,
   menu_image
) values ( - 23,
           'メンチカツバーガー',
           5,
           400,
           230,
           'menuImages/hamburger.jpg' );
insert into menus (
   menu_id,
   menu_name,
   category_id,
   menu_price,
   sort_order,
   menu_image
) values ( - 24,
           'テリヤキチキンバーガー',
           5,
           500,
           240,
           'menuImages/hamburger.jpg' );
insert into menus (
   menu_id,
   menu_name,
   category_id,
   menu_price,
   sort_order,
   menu_image
) values ( - 25,
           'ロースカツサンド',
           5,
           600,
           250,
           'menuImages/hamburger.jpg' );
insert into menus (
   menu_id,
   menu_name,
   category_id,
   menu_price,
   sort_order,
   menu_image
) values ( - 26,
           'エッグサンド',
           5,
           500,
           260,
           'menuImages/hamburger.jpg' );
insert into menus (
   menu_id,
   menu_name,
   category_id,
   menu_price,
   sort_order,
   menu_image
) values ( - 27,
           'フィッシュバーガー',
           5,
           400,
           270,
           'menuImages/hamburger.jpg' );
insert into menus (
   menu_id,
   menu_name,
   category_id,
   menu_price,
   sort_order,
   menu_image
) values ( - 28,
           'ヤンニョムチキンバーガー',
           5,
           500,
           280,
           'menuImages/hamburger.jpg' );
insert into menus (
   menu_id,
   menu_name,
   category_id,
   menu_price,
   sort_order,
   menu_image
) values ( - 29,
           'チキンカツサンド',
           5,
           500,
           290,
           'menuImages/hamburger.jpg' );
insert into menus (
   menu_id,
   menu_name,
   category_id,
   menu_price,
   sort_order,
   menu_image
) values ( - 30,
           'スモークサーモンサンド',
           5,
           500,
           300,
           'menuImages/hamburger.jpg' );
insert into menus (
   menu_id,
   menu_name,
   category_id,
   menu_price,
   sort_order,
   menu_image
) values ( - 31,
           'フライドポテト',
           6,
           250,
           310,
           'menuImages/hamburger.jpg' );
insert into menus (
   menu_id,
   menu_name,
   category_id,
   menu_price,
   sort_order,
   menu_image
) values ( - 32,
           'ハッシュドポテト',
           6,
           300,
           320,
           'menuImages/hamburger.jpg' );
insert into menus (
   menu_id,
   menu_name,
   category_id,
   menu_price,
   sort_order,
   menu_image
) values ( - 33,
           'ウェッジポテト',
           6,
           250,
           330,
           'menuImages/hamburger.jpg' );
insert into menus (
   menu_id,
   menu_name,
   category_id,
   menu_price,
   sort_order,
   menu_image
) values ( - 34,
           'バレルハッシュポテト',
           6,
           250,
           340,
           'menuImages/hamburger.jpg' );
insert into menus (
   menu_id,
   menu_name,
   category_id,
   menu_price,
   sort_order,
   menu_image
) values ( - 35,
           'ラティスカットポテト',
           6,
           250,
           350,
           'menuImages/hamburger.jpg' );
insert into menus (
   menu_id,
   menu_name,
   category_id,
   menu_price,
   sort_order,
   menu_image
) values ( - 36,
           'チキンナゲット',
           7,
           250,
           360,
           'menuImages/hamburger.jpg' );
insert into menus (
   menu_id,
   menu_name,
   category_id,
   menu_price,
   sort_order,
   menu_image
) values ( - 37,
           'フライドチキン',
           7,
           250,
           370,
           'menuImages/hamburger.jpg' );
insert into menus (
   menu_id,
   menu_name,
   category_id,
   menu_price,
   sort_order,
   menu_image
) values ( - 38,
           'スパイシーチキン',
           7,
           250,
           380,
           'menuImages/hamburger.jpg' );
insert into menus (
   menu_id,
   menu_name,
   category_id,
   menu_price,
   sort_order,
   menu_image
) values ( - 39,
           'ささみ',
           7,
           200,
           390,
           'menuImages/hamburger.jpg' );
insert into menus (
   menu_id,
   menu_name,
   category_id,
   menu_price,
   sort_order,
   menu_image
) values ( - 40,
           'クリスポーチキン',
           7,
           250,
           400,
           'menuImages/hamburger.jpg' );
insert into menus (
   menu_id,
   menu_name,
   category_id,
   menu_price,
   sort_order,
   menu_image
) values ( - 41,
           'シーザーサラダ',
           8,
           250,
           410,
           'menuImages/hamburger.jpg' );
insert into menus (
   menu_id,
   menu_name,
   category_id,
   menu_price,
   sort_order,
   menu_image
) values ( - 42,
           'ポテトサラダ',
           8,
           250,
           420,
           'menuImages/hamburger.jpg' );
insert into menus (
   menu_id,
   menu_name,
   category_id,
   menu_price,
   sort_order,
   menu_image
) values ( - 43,
           'コールスロー',
           8,
           250,
           430,
           'menuImages/hamburger.jpg' );
insert into menus (
   menu_id,
   menu_name,
   category_id,
   menu_price,
   sort_order,
   menu_image
) values ( - 44,
           'エスニックサラダ',
           8,
           250,
           440,
           'menuImages/hamburger.jpg' );
insert into menus (
   menu_id,
   menu_name,
   category_id,
   menu_price,
   sort_order,
   menu_image
) values ( - 45,
           'グリーンサラダ',
           8,
           250,
           450,
           'menuImages/hamburger.jpg' );
insert into menus (
   menu_id,
   menu_name,
   category_id,
   menu_price,
   sort_order,
   menu_image
) values ( - 46,
           'ケチャップ',
           9,
           50,
           460,
           'menuImages/hamburger.jpg' );
insert into menus (
   menu_id,
   menu_name,
   category_id,
   menu_price,
   sort_order,
   menu_image
) values ( - 47,
           'バーベキューソース',
           9,
           50,
           470,
           'menuImages/hamburger.jpg' );
insert into menus (
   menu_id,
   menu_name,
   category_id,
   menu_price,
   sort_order,
   menu_image
) values ( - 48,
           'マスタード',
           9,
           50,
           480,
           'menuImages/hamburger.jpg' );
insert into menus (
   menu_id,
   menu_name,
   category_id,
   menu_price,
   sort_order,
   menu_image
) values ( - 49,
           'チーズソース',
           9,
           50,
           490,
           'menuImages/hamburger.jpg' );
insert into menus (
   menu_id,
   menu_name,
   category_id,
   menu_price,
   sort_order,
   menu_image
) values ( - 50,
           '粉チーズ',
           9,
           50,
           500,
           'menuImages/hamburger.jpg' );
insert into menus (
   menu_id,
   menu_name,
   category_id,
   menu_price,
   sort_order,
   menu_image
) values ( - 51,
           'コカコーラ',
           10,
           200,
           510,
           'menuImages/hamburger.jpg' );
insert into menus (
   menu_id,
   menu_name,
   category_id,
   menu_price,
   sort_order,
   menu_image
) values ( - 52,
           'ペプシコーラ',
           10,
           200,
           520,
           'menuImages/hamburger.jpg' );
insert into menus (
   menu_id,
   menu_name,
   category_id,
   menu_price,
   sort_order,
   menu_image
) values ( - 53,
           'ドクターペッパー',
           10,
           200,
           530,
           'menuImages/hamburger.jpg' );
insert into menus (
   menu_id,
   menu_name,
   category_id,
   menu_price,
   sort_order,
   menu_image
) values ( - 54,
           'オレンジジュース',
           10,
           200,
           540,
           'menuImages/hamburger.jpg' );
insert into menus (
   menu_id,
   menu_name,
   category_id,
   menu_price,
   sort_order,
   menu_image
) values ( - 55,
           'リンゴジュース',
           10,
           200,
           550,
           'menuImages/hamburger.jpg' );
insert into menus (
   menu_id,
   menu_name,
   category_id,
   menu_price,
   sort_order,
   menu_image
) values ( - 56,
           'レモネード',
           10,
           200,
           560,
           'menuImages/hamburger.jpg' );
insert into menus (
   menu_id,
   menu_name,
   category_id,
   menu_price,
   sort_order,
   menu_image
) values ( - 57,
           'ソーダ',
           10,
           200,
           570,
           'menuImages/hamburger.jpg' );
insert into menus (
   menu_id,
   menu_name,
   category_id,
   menu_price,
   sort_order,
   menu_image
) values ( - 58,
           'ホワイトジュース',
           10,
           200,
           580,
           'menuImages/hamburger.jpg' );
insert into menus (
   menu_id,
   menu_name,
   category_id,
   menu_price,
   sort_order,
   menu_image
) values ( - 59,
           'ホワイトソーダ',
           10,
           200,
           590,
           'menuImages/hamburger.jpg' );
insert into menus (
   menu_id,
   menu_name,
   category_id,
   menu_price,
   sort_order,
   menu_image
) values ( - 60,
           'コーヒー',
           11,
           400,
           600,
           'menuImages/hamburger.jpg' );
insert into menus (
   menu_id,
   menu_name,
   category_id,
   menu_price,
   sort_order,
   menu_image
) values ( - 61,
           'アメリカンコーヒー',
           11,
           400,
           610,
           'menuImages/hamburger.jpg' );
insert into menus (
   menu_id,
   menu_name,
   category_id,
   menu_price,
   sort_order,
   menu_image
) values ( - 62,
           'カフェラテ',
           11,
           400,
           620,
           'menuImages/hamburger.jpg' );
insert into menus (
   menu_id,
   menu_name,
   category_id,
   menu_price,
   sort_order,
   menu_image
) values ( - 63,
           'カプチーノ',
           11,
           400,
           630,
           'menuImages/hamburger.jpg' );
insert into menus (
   menu_id,
   menu_name,
   category_id,
   menu_price,
   sort_order,
   menu_image
) values ( - 64,
           'エスプレッソコーヒー',
           11,
           400,
           640,
           'menuImages/hamburger.jpg' );
insert into menus (
   menu_id,
   menu_name,
   category_id,
   menu_price,
   sort_order,
   menu_image
) values ( - 65,
           'ビール',
           12,
           400,
           650,
           'menuImages/hamburger.jpg' );
insert into menus (
   menu_id,
   menu_name,
   category_id,
   menu_price,
   sort_order,
   menu_image
) values ( - 66,
           'コークハイ',
           12,
           400,
           660,
           'menuImages/hamburger.jpg' );
insert into menus (
   menu_id,
   menu_name,
   category_id,
   menu_price,
   sort_order,
   menu_image
) values ( - 67,
           'ハイボール',
           12,
           400,
           670,
           'menuImages/hamburger.jpg' );
insert into menus (
   menu_id,
   menu_name,
   category_id,
   menu_price,
   sort_order,
   menu_image
) values ( - 68,
           '赤ワイン',
           12,
           500,
           680,
           'menuImages/hamburger.jpg' );
insert into menus (
   menu_id,
   menu_name,
   category_id,
   menu_price,
   sort_order,
   menu_image
) values ( - 69,
           '白ワイン',
           12,
           500,
           690,
           'menuImages/hamburger.jpg' );

insert into options (
   menu_id,
   option_price,
   option_name
) values ( - 1,
           80,
           'チーズ追加' );
insert into options (
   menu_id,
   option_price,
   option_name
) values ( - 1,
           120,
           'ベーコン追加' );
insert into options (
   menu_id,
   option_price,
   option_name
) values ( - 2,
           250,
           'パティ追加' );
insert into options (
   menu_id,
   option_price,
   option_name
) values ( - 2,
           30,
           'ピクルス増量' );
insert into options (
   menu_id,
   option_price,
   option_name
) values ( - 3,
           80,
           'チェダーチーズ1枚追加' );
insert into options (
   menu_id,
   option_price,
   option_name
) values ( - 3,
           0,
           'オニオン抜き' );
insert into options (
   menu_id,
   option_price,
   option_name
) values ( - 4,
           250,
           'パティ1枚追加' );
insert into options (
   menu_id,
   option_price,
   option_name
) values ( - 4,
           80,
           'チーズ追加' );
insert into options (
   menu_id,
   option_price,
   option_name
) values ( - 4,
           120,
           'ベーコン追加' );
insert into options (
   menu_id,
   option_price,
   option_name
) values ( - 5,
           100,
           'エッグトッピング' );
insert into options (
   menu_id,
   option_price,
   option_name
) values ( - 5,
           80,
           'チーズ増量' );
insert into options (
   menu_id,
   option_price,
   option_name
) values ( - 6,
           150,
           'エビトッピング' );
insert into options (
   menu_id,
   option_price,
   option_name
) values ( - 6,
           50,
           'レタス増量' );
insert into options (
   menu_id,
   option_price,
   option_name
) values ( - 7,
           250,
           'パティ追加' );
insert into options (
   menu_id,
   option_price,
   option_name
) values ( - 7,
           80,
           'トマト追加' );
insert into options (
   menu_id,
   option_price,
   option_name
) values ( - 8,
           80,
           'チーズ追加' );
insert into options (
   menu_id,
   option_price,
   option_name
) values ( - 8,
           120,
           'ベーコン追加' );
insert into options (
   menu_id,
   option_price,
   option_name
) values ( - 9,
           250,
           'パティ追加' );
insert into options (
   menu_id,
   option_price,
   option_name
) values ( - 9,
           100,
           'エッグ1個追加' );
insert into options (
   menu_id,
   option_price,
   option_name
) values ( - 10,
           30,
           'マヨネーズ多め' );
insert into options (
   menu_id,
   option_price,
   option_name
) values ( - 10,
           100,
           'エッグトッピング' );
insert into options (
   menu_id,
   option_price,
   option_name
) values ( - 11,
           100,
           '追いチーズソース' );
insert into options (
   menu_id,
   option_price,
   option_name
) values ( - 11,
           80,
           'ハラペーニョ追加' );
insert into options (
   menu_id,
   option_price,
   option_name
) values ( - 12,
           100,
           '追いチーズソース' );
insert into options (
   menu_id,
   option_price,
   option_name
) values ( - 12,
           250,
           'パティ追加' );
insert into options (
   menu_id,
   option_price,
   option_name
) values ( - 13,
           50,
           'ガーリックチップ追加' );
insert into options (
   menu_id,
   option_price,
   option_name
) values ( - 13,
           150,
           '厚切りベーコン追加' );
insert into options (
   menu_id,
   option_price,
   option_name
) values ( - 14,
           100,
           'オニオンフライ追加' );
insert into options (
   menu_id,
   option_price,
   option_name
) values ( - 14,
           80,
           'チーズ追加' );
insert into options (
   menu_id,
   option_price,
   option_name
) values ( - 15,
           120,
           'ベーコン追加' );
insert into options (
   menu_id,
   option_price,
   option_name
) values ( - 15,
           250,
           'パティ追加' );
insert into options (
   menu_id,
   option_price,
   option_name
) values ( - 16,
           80,
           'チーズ追加' );
insert into options (
   menu_id,
   option_price,
   option_name
) values ( - 16,
           50,
           '追いオニオン' );
insert into options (
   menu_id,
   option_price,
   option_name
) values ( - 17,
           100,
           'ミートソース追加' );
insert into options (
   menu_id,
   option_price,
   option_name
) values ( - 17,
           250,
           'パティ追加' );
insert into options (
   menu_id,
   option_price,
   option_name
) values ( - 18,
           80,
           'チーズ追加' );
insert into options (
   menu_id,
   option_price,
   option_name
) values ( - 18,
           50,
           'スパイシーソースに変更' );
insert into options (
   menu_id,
   option_price,
   option_name
) values ( - 19,
           50,
           'レタス増量' );
insert into options (
   menu_id,
   option_price,
   option_name
) values ( - 19,
           80,
           'チーズ追加' );
insert into options (
   menu_id,
   option_price,
   option_name
) values ( - 20,
           50,
           'タルタルソース増量' );
insert into options (
   menu_id,
   option_price,
   option_name
) values ( - 20,
           100,
           'エッグトッピング' );
insert into options (
   menu_id,
   option_price,
   option_name
) values ( - 21,
           150,
           'アボカドトッピング' );
insert into options (
   menu_id,
   option_price,
   option_name
) values ( - 21,
           80,
           'チーズ追加' );
insert into options (
   menu_id,
   option_price,
   option_name
) values ( - 22,
           200,
           'ソイパティ1枚追加' );
insert into options (
   menu_id,
   option_price,
   option_name
) values ( - 22,
           80,
           '野菜増量' );
insert into options (
   menu_id,
   option_price,
   option_name
) values ( - 23,
           20,
           'マスタード追加' );
insert into options (
   menu_id,
   option_price,
   option_name
) values ( - 23,
           80,
           'チーズ追加' );
insert into options (
   menu_id,
   option_price,
   option_name
) values ( - 24,
           100,
           'エッグトッピング' );
insert into options (
   menu_id,
   option_price,
   option_name
) values ( - 24,
           50,
           'レタス増量' );
insert into options (
   menu_id,
   option_price,
   option_name
) values ( - 25,
           50,
           'キャベツ増量' );
insert into options (
   menu_id,
   option_price,
   option_name
) values ( - 25,
           80,
           'チーズ追加' );
insert into options (
   menu_id,
   option_price,
   option_name
) values ( - 26,
           150,
           '厚切りベーコン追加' );
insert into options (
   menu_id,
   option_price,
   option_name
) values ( - 26,
           80,
           'ハラペーニョ追加' );
insert into options (
   menu_id,
   option_price,
   option_name
) values ( - 27,
           50,
           'タルタルソース増量' );
insert into options (
   menu_id,
   option_price,
   option_name
) values ( - 27,
           80,
           'チーズ追加' );
insert into options (
   menu_id,
   option_price,
   option_name
) values ( - 28,
           50,
           '追いヤンニョムソース' );
insert into options (
   menu_id,
   option_price,
   option_name
) values ( - 28,
           30,
           'マヨネーズ追加' );
insert into options (
   menu_id,
   option_price,
   option_name
) values ( - 28,
           300,
           '辛さMAX' );
insert into options (
   menu_id,
   option_price,
   option_name
) values ( - 29,
           50,
           'キャベツ増量' );
insert into options (
   menu_id,
   option_price,
   option_name
) values ( - 29,
           80,
           'チーズ追加' );
insert into options (
   menu_id,
   option_price,
   option_name
) values ( - 30,
           100,
           'クリームチーズ追加' );
insert into options (
   menu_id,
   option_price,
   option_name
) values ( - 30,
           50,
           'オニオンスライス追加' );
insert into options (
   menu_id,
   option_price,
   option_name
) values ( - 31,
           100,
           'Lサイズへ変更' );
insert into options (
   menu_id,
   option_price,
   option_name
) values ( - 32,
           50,
           'ケチャップ追加' );
insert into options (
   menu_id,
   option_price,
   option_name
) values ( - 33,
           100,
           'チーズソース追加' );
insert into options (
   menu_id,
   option_price,
   option_name
) values ( - 34,
           100,
           'Lサイズへ変更' );
insert into options (
   menu_id,
   option_price,
   option_name
) values ( - 35,
           30,
           'ガーリックパウダー追加' );
insert into options (
   menu_id,
   option_price,
   option_name
) values ( - 36,
           150,
           '5個増量' );
insert into options (
   menu_id,
   option_price,
   option_name
) values ( - 37,
           50,
           '辛口スパイス追加' );
insert into options (
   menu_id,
   option_price,
   option_name
) values ( - 38,
           50,
           'タルタルソース追加' );
insert into options (
   menu_id,
   option_price,
   option_name
) values ( - 39,
           20,
           'レモン果汁追加' );
insert into options (
   menu_id,
   option_price,
   option_name
) values ( - 40,
           50,
           'マスタードソース追加' );
insert into options (
   menu_id,
   option_price,
   option_name
) values ( - 41,
           30,
           'クルトン増量' );
insert into options (
   menu_id,
   option_price,
   option_name
) values ( - 42,
           50,
           'ベーコンビット追加' );
insert into options (
   menu_id,
   option_price,
   option_name
) values ( - 43,
           50,
           'コーン追加' );
insert into options (
   menu_id,
   option_price,
   option_name
) values ( - 44,
           100,
           'パクチー追加' );
insert into options (
   menu_id,
   option_price,
   option_name
) values ( - 45,
           50,
           'ドレッシング2倍' );
insert into options (
   menu_id,
   option_price,
   option_name
) values ( - 51,
           50,
           'Lサイズへ変更' );
insert into options (
   menu_id,
   option_price,
   option_name
) values ( - 52,
           50,
           'Lサイズへ変更' );
insert into options (
   menu_id,
   option_price,
   option_name
) values ( - 53,
           50,
           'Lサイズへ変更' );
insert into options (
   menu_id,
   option_price,
   option_name
) values ( - 54,
           50,
           'Lサイズへ変更' );
insert into options (
   menu_id,
   option_price,
   option_name
) values ( - 55,
           50,
           'Lサイズへ変更' );
insert into options (
   menu_id,
   option_price,
   option_name
) values ( - 56,
           30,
           'カットレモン追加' );
insert into options (
   menu_id,
   option_price,
   option_name
) values ( - 57,
           150,
           'バニラアイス追加(フロート)' );
insert into options (
   menu_id,
   option_price,
   option_name
) values ( - 58,
           50,
           'Lサイズへ変更' );
insert into options (
   menu_id,
   option_price,
   option_name
) values ( - 59,
           150,
           'バニラアイス追加(フロート)' );
insert into options (
   menu_id,
   option_price,
   option_name
) values ( - 60,
           70,
           'ホイップクリーム追加' );
insert into options (
   menu_id,
   option_price,
   option_name
) values ( - 61,
           50,
           'Lサイズへ変更' );
insert into options (
   menu_id,
   option_price,
   option_name
) values ( - 62,
           50,
           'キャラメルシロップ追加' );
insert into options (
   menu_id,
   option_price,
   option_name
) values ( - 63,
           30,
           'シナモンパウダー追加' );
insert into options (
   menu_id,
   option_price,
   option_name
) values ( - 64,
           100,
           'ショット追加' );
insert into options (
   menu_id,
   option_price,
   option_name
) values ( - 65,
           150,
           'おつまみナッツ付き' );
insert into options (
   menu_id,
   option_price,
   option_name
) values ( - 66,
           30,
           'カットレモン追加' );
insert into options (
   menu_id,
   option_price,
   option_name
) values ( - 67,
           100,
           '濃いめに変更' );
insert into options (
   menu_id,
   option_price,
   option_name
) values ( - 68,
           300,
           'チーズ盛り合わせ付き' );
insert into options (
   menu_id,
   option_price,
   option_name
) values ( - 69,
           250,
           'オリーブ盛り合わせ付き' );

insert into device_types values ( 1,
                                  '管理用端末' );
insert into device_types values ( 2,
                                  '卓上端末' );
insert into device_types values ( 3,
                                  'キッチン端末' );
insert into device_types values ( 4,
                                  'ホール端末' );
insert into device_types values ( 5,
                                  'レジ端末' );
insert into device_types values ( 6,
                                  '顧客人数入力端末' );

insert into tables (
   table_id,
   table_name,
   table_capacity
) values ( 1,
           'Table_1',
           1 );
insert into tables (
   table_id,
   table_name,
   table_capacity
) values ( 2,
           'Table_2',
           2 );
insert into tables (
   table_id,
   table_name,
   table_capacity
) values ( 3,
           'Table_3',
           3 );
insert into tables (
   table_id,
   table_name,
   table_capacity
) values ( 1001,
           'ex2提出用 テーブル1',
           1 );
insert into tables (
   table_id,
   table_name,
   table_capacity
) values ( 1002,
           'ex2提出用 テーブル2',
           2 );
insert into tables (
   table_id,
   table_name,
   table_capacity
) values ( 1003,
           'ex2提出用 テーブル3',
           3 );

insert into devices (
   device_id,
   device_name,
   device_type_id
) values ( hextoraw('019c597914c971afaef88efb9bab70f2'),
           'G04_Manager',
           1 );
insert into devices (
   device_id,
   device_name,
   device_type_id,
   table_id
) values ( hextoraw('1632a379e573466e93341948b3438dd2'),
           'G04_Table1',
           2,
           1 );
insert into devices (
   device_id,
   device_name,
   device_type_id
) values ( hextoraw('b578592e2260416cac60b90d310b55bd'),
           'G04_Kitchen1',
           3 );
insert into devices (
   device_id,
   device_name,
   device_type_id
) values ( hextoraw('e5cf6c8232eb4e548290f83f5e9e764b'),
           'G04_Hall1',
           4 );
insert into devices (
   device_id,
   device_name,
   device_type_id
) values ( hextoraw('c33d3967daa0439799d6f76ec0128bb3'),
           'G04_Register1',
           5 );
insert into devices (
   device_id,
   device_name,
   device_type_id
) values ( hextoraw('75e890cb117c4647b00d29c8b689da3b'),
           'G04_Entrance1',
           6 );

-- 以上デモ用デバイス
insert into devices (
   device_id,
   device_name,
   device_type_id
) values ( hextoraw('00000000000000000000000000000000'),
           '開発環境',
           1 );
-- insert into devices (
insert into devices (
   device_id,
   device_name,
   device_type_id,
   table_id
) values ( hextoraw('00000000000000000000000000000014'),
           'devices_20 Table1',
           2,
           1 );
insert into devices (
   device_id,
   device_name,
   device_type_id,
   table_id
) values ( hextoraw('00000000000000000000000000000015'),
           'devices_21 Table2',
           2,
           2 );
insert into devices (
   device_id,
   device_name,
   device_type_id
) values ( hextoraw('0000000000000000000000000000001e'),
           'devices_30 Kitchen1',
           3 );
insert into devices (
   device_id,
   device_name,
   device_type_id
) values ( hextoraw('0000000000000000000000000000001f'),
           'devices_31 Kitchen2',
           3 );
insert into devices (
   device_id,
   device_name,
   device_type_id
) values ( hextoraw('00000000000000000000000000000028'),
           'devices_40 Hall1',
           4 );
insert into devices (
   device_id,
   device_name,
   device_type_id
) values ( hextoraw('00000000000000000000000000000029'),
           'devices_41 Hall2',
           4 );
commit;
insert into devices (
   device_id,
   device_name,
   device_type_id
) values ( hextoraw('00000000000000000000000000000032'),
           'devices_50 Register1',
           5 );
insert into devices (
   device_id,
   device_name,
   device_type_id
) values ( hextoraw('00000000000000000000000000000033'),
           'devices_51 Register2',
           5 );
-- 以下ex2提出用、開発で使用禁止
insert into devices (
   device_id,
   device_name,
   device_type_id,
   table_id
) values ( hextoraw('bdc7985882904bb9b5735169036cbb61'), -- bdc79858-8290-4bb9-b573-5169036cbb61
           'ex2提出用卓上端末 001',
           2,
           1001 );
insert into devices (
   device_id,
   device_name,
   device_type_id,
   table_id
) values ( hextoraw('57978fdb6abd4825befc5ae0114eeaa9'), -- 57978fdb-6abd-4825-befc-5ae0114eeaa9
           'ex2提出用卓上端末 002',
           2,
           1002 );
insert into devices (
   device_id,
   device_name,
   device_type_id,
   table_id
) values ( hextoraw('d2ce442feac54265b3c617dc65fd660c'), -- d2ce442f-eac5-4265-b3c6-17dc65fd660c
           'ex2提出用 卓上端末 003',
           2,
           1003 );

insert into customer_statuses values ( 1,
                                       '未着席' );
insert into customer_statuses values ( 2,
                                       '飲食中' );
insert into customer_statuses values ( 3,
                                       '飲食終了' );

insert into order_statuses values ( 1,
                                    '調理待ち',
                                    '準備中' );
insert into order_statuses values ( 2,
                                    '配膳待ち',
                                    '準備中' );
insert into order_statuses values ( 3,
                                    '提供済み',
                                    '提供済み' );

insert into users (
   username,
   password_hash,
   salt
) values ( 'admin00',
           hextoraw('8BF6C47EAF2BBA0AC2A08546033D18B57BD73CEC24A6B17A93B9BF0AA14F6376'),
           hextoraw('A541EBBA4D768C092CEB21CCD0E0FC7C') );
