-- V3__seed_core_hadith_data.sql
-- بيانات أولية متوافقة مع جداول مشروع Ahadith-spring.
-- تحتوي على:
-- 10 أحاديث من صحيح البخاري
-- 10 أحاديث من صحيح مسلم
-- 10 أحاديث منتشرة لا تصح
--
-- ملاحظات:
-- 1) الملف يعتمد على الجداول الموجودة في V1__Create_Tables.sql.
-- 2) لا يعبئ normal_text/search_text/search_vector لأن Triggers في V2 تفعل ذلك تلقائيا.
-- 3) الإدخال idempotent قدر الإمكان، أي يمكن تشغيله أكثر من مرة دون تكرار الأحاديث الأساسية.
-- 4) مصادر التحقق: الدرر السنية / الموسوعة الحديثية وأحاديث منتشرة لا تصح.

BEGIN;

CREATE OR REPLACE FUNCTION public.arab_norm(p_text text)
RETURNS text
LANGUAGE plpgsql
IMMUTABLE
AS $$
DECLARE
  v_text text;
BEGIN
  IF p_text IS NULL THEN
    RETURN NULL;
  END IF;

  v_text := p_text;

  v_text := replace(v_text, 'أ', 'ا');
  v_text := replace(v_text, 'إ', 'ا');
  v_text := replace(v_text, 'آ', 'ا');
  v_text := replace(v_text, 'ٱ', 'ا');
  v_text := replace(v_text, 'ى', 'ي');
  v_text := replace(v_text, 'ة', 'ه');
  v_text := replace(v_text, 'ؤ', 'و');
  v_text := replace(v_text, 'ئ', 'ي');

  v_text := regexp_replace(v_text, $re$[ًٌٍَُِّْٰـۖۗۘۙۚۛۜ۝۞ۣ۟۠ۡۢۤۥۦۧۨ۩۪ۭ۫۬ۮۯ]$re$, '', 'g');
  v_text := regexp_replace(v_text, $re$[^[:alnum:]ء-ي ]$re$, ' ', 'g');
  v_text := regexp_replace(v_text, $re$[[:space:]]+$re$, ' ', 'g');

  RETURN btrim(v_text);
END;
$$;

-- =========================
-- RULINGS
-- =========================
INSERT INTO public.ruling (name) VALUES
('صحيح'),
('حسن صحيح'),
('ضعيف'),
('موضوع'),
('لا يصح'),
('منكر'),
('ليس له أصل'),
('مكذوب')
ON CONFLICT (name) DO NOTHING;

-- =========================
-- MUHADDITHS
-- =========================
INSERT INTO public.muhaddiths (name, gender, about) VALUES
('الإمام البخاري', 'male', 'محمد بن إسماعيل البخاري، صاحب صحيح البخاري، من أئمة الحديث وحفاظه.'),
('الإمام مسلم', 'male', 'مسلم بن الحجاج النيسابوري، صاحب صحيح مسلم، من كبار أئمة الحديث.')
ON CONFLICT (name) DO NOTHING;

-- =========================
-- BOOKS
-- =========================
INSERT INTO public.books (name, muhaddith)
SELECT 'صحيح البخاري', id FROM public.muhaddiths WHERE name = 'الإمام البخاري'
ON CONFLICT (name) DO NOTHING;

INSERT INTO public.books (name, muhaddith)
SELECT 'صحيح مسلم', id FROM public.muhaddiths WHERE name = 'الإمام مسلم'
ON CONFLICT (name) DO NOTHING;

-- =========================
-- RAWIS
-- =========================
INSERT INTO public.rawis (name, gender, about) VALUES
('عمر بن الخطاب', 'male', 'أمير المؤمنين، ثاني الخلفاء الراشدين، ومن كبار الصحابة.'),
('عبد الله بن عمر', 'male', 'صحابي جليل، من المكثرين في الرواية عن النبي صلى الله عليه وسلم.'),
('أنس بن مالك', 'male', 'خادم رسول الله صلى الله عليه وسلم، ومن المكثرين في رواية الحديث.'),
('النعمان بن بشير', 'male', 'صحابي جليل، روى أحاديث في الحلال والحرام والفتن وغيرها.'),
('معاوية بن أبي سفيان', 'male', 'صحابي جليل وكاتب من كتاب الوحي.'),
('جابر بن عبد الله', 'male', 'صحابي جليل من المكثرين في الرواية.'),
('أبو هريرة', 'male', 'صحابي جليل، أكثر الصحابة رواية للحديث.'),
('أبو سعيد الخدري', 'male', 'صحابي جليل من فقهاء الصحابة ومكثري الرواية.'),
('أبو رقية تميم الداري', 'male', 'صحابي جليل، يروى عنه حديث الدين النصيحة.'),
('أبو مالك الأشعري', 'male', 'صحابي جليل، روى حديث الطهور شطر الإيمان.'),
('عبد الله بن مسعود', 'male', 'صحابي جليل، من فقهاء الصحابة وقرائهم.'),
('أبو ذر الغفاري', 'male', 'صحابي جليل مشهور بالزهد والصدق.')
ON CONFLICT (name) DO NOTHING;

-- =========================
-- TOPICS
-- =========================
INSERT INTO public.topics (name) VALUES
('الإخلاص والنية'),
('أركان الإسلام'),
('الأخلاق'),
('الحلال والحرام'),
('العلم'),
('النصرة والعدل'),
('الغضب'),
('الوقت والصحة'),
('آداب اللسان'),
('الأمر بالمعروف والنهي عن المنكر'),
('الطهارة'),
('الأخوة الإسلامية'),
('الصبر والرضا'),
('الصدقة'),
('الدعاء'),
('العمل الصالح بعد الموت'),
('أحاديث منتشرة لا تصح')
ON CONFLICT (name) DO NOTHING;

-- =========================
-- EXPLAINING + AHADITH: SAHIH AL-BUKHARI
-- =========================

INSERT INTO public.explaining (text)
SELECT $q$يبين الحديث أن مدار قبول الأعمال على النية، وأن صورة العمل وحدها لا تكفي حتى يكون القصد لله تعالى. وفيه أصل عظيم في الإخلاص ومحاسبة القلب قبل العمل.$q$
WHERE NOT EXISTS (SELECT 1 FROM public.explaining WHERE text = $q$يبين الحديث أن مدار قبول الأعمال على النية، وأن صورة العمل وحدها لا تكفي حتى يكون القصد لله تعالى. وفيه أصل عظيم في الإخلاص ومحاسبة القلب قبل العمل.$q$);

INSERT INTO public.ahadith (type, text, hadith_number, ruling, rawi, book, sanad, explaining)
SELECT 'marfu',
$q$إنما الأعمال بالنيات، وإنما لكل امرئ ما نوى، فمن كانت هجرته إلى الله ورسوله فهجرته إلى الله ورسوله، ومن كانت هجرته لدنيا يصيبها أو امرأة ينكحها فهجرته إلى ما هاجر إليه.$q$,
1,
(SELECT id FROM public.ruling WHERE name = 'صحيح'),
(SELECT id FROM public.rawis WHERE name = 'عمر بن الخطاب'),
(SELECT id FROM public.books WHERE name = 'صحيح البخاري'),
'أخرجه البخاري في صحيحه، وأخرجه مسلم بمعناه.',
(SELECT id FROM public.explaining WHERE text = $q$يبين الحديث أن مدار قبول الأعمال على النية، وأن صورة العمل وحدها لا تكفي حتى يكون القصد لله تعالى. وفيه أصل عظيم في الإخلاص ومحاسبة القلب قبل العمل.$q$ LIMIT 1)
WHERE NOT EXISTS (
  SELECT 1 FROM public.ahadith
  WHERE book = (SELECT id FROM public.books WHERE name = 'صحيح البخاري')
    AND hadith_number = 1
);

INSERT INTO public.explaining (text)
SELECT $q$يجمع الحديث أصول الإسلام الظاهرة، ويبين أن هذه الأركان هي أساس بناء الدين العملي الذي يقوم عليه التزام المسلم.$q$
WHERE NOT EXISTS (SELECT 1 FROM public.explaining WHERE text = $q$يجمع الحديث أصول الإسلام الظاهرة، ويبين أن هذه الأركان هي أساس بناء الدين العملي الذي يقوم عليه التزام المسلم.$q$);

INSERT INTO public.ahadith (type, text, hadith_number, ruling, rawi, book, sanad, explaining)
SELECT 'marfu',
$q$بني الإسلام على خمس: شهادة أن لا إله إلا الله وأن محمدا رسول الله، وإقام الصلاة، وإيتاء الزكاة، والحج، وصوم رمضان.$q$,
8,
(SELECT id FROM public.ruling WHERE name = 'صحيح'),
(SELECT id FROM public.rawis WHERE name = 'عبد الله بن عمر'),
(SELECT id FROM public.books WHERE name = 'صحيح البخاري'),
'متفق عليه من حديث عبد الله بن عمر رضي الله عنهما.',
(SELECT id FROM public.explaining WHERE text = $q$يجمع الحديث أصول الإسلام الظاهرة، ويبين أن هذه الأركان هي أساس بناء الدين العملي الذي يقوم عليه التزام المسلم.$q$ LIMIT 1)
WHERE NOT EXISTS (
  SELECT 1 FROM public.ahadith
  WHERE book = (SELECT id FROM public.books WHERE name = 'صحيح البخاري')
    AND hadith_number = 8
);

INSERT INTO public.explaining (text)
SELECT $q$يدل الحديث على كمال الإيمان الواجب بمحبة الخير للمسلمين، وأن سلامة الصدر والإيثار من علامات صدق الإيمان.$q$
WHERE NOT EXISTS (SELECT 1 FROM public.explaining WHERE text = $q$يدل الحديث على كمال الإيمان الواجب بمحبة الخير للمسلمين، وأن سلامة الصدر والإيثار من علامات صدق الإيمان.$q$);

INSERT INTO public.ahadith (type, text, hadith_number, ruling, rawi, book, sanad, explaining)
SELECT 'marfu',
$q$لا يؤمن أحدكم حتى يحب لأخيه ما يحب لنفسه.$q$,
13,
(SELECT id FROM public.ruling WHERE name = 'صحيح'),
(SELECT id FROM public.rawis WHERE name = 'أنس بن مالك'),
(SELECT id FROM public.books WHERE name = 'صحيح البخاري'),
'متفق عليه من حديث أنس بن مالك رضي الله عنه.',
(SELECT id FROM public.explaining WHERE text = $q$يدل الحديث على كمال الإيمان الواجب بمحبة الخير للمسلمين، وأن سلامة الصدر والإيثار من علامات صدق الإيمان.$q$ LIMIT 1)
WHERE NOT EXISTS (
  SELECT 1 FROM public.ahadith
  WHERE book = (SELECT id FROM public.books WHERE name = 'صحيح البخاري')
    AND hadith_number = 13
);

INSERT INTO public.explaining (text)
SELECT $q$يبين الحديث أن الحلال والحرام منهما ما هو واضح، وبينهما أمور مشتبهة، وأن الورع ترك المشتبهات حفظا للدين والعرض.$q$
WHERE NOT EXISTS (SELECT 1 FROM public.explaining WHERE text = $q$يبين الحديث أن الحلال والحرام منهما ما هو واضح، وبينهما أمور مشتبهة، وأن الورع ترك المشتبهات حفظا للدين والعرض.$q$);

INSERT INTO public.ahadith (type, text, hadith_number, ruling, rawi, book, sanad, explaining)
SELECT 'marfu',
$q$الحلال بين والحرام بين، وبينهما أمور مشتبهات لا يعلمهن كثير من الناس، فمن اتقى الشبهات فقد استبرأ لدينه وعرضه.$q$,
52,
(SELECT id FROM public.ruling WHERE name = 'صحيح'),
(SELECT id FROM public.rawis WHERE name = 'النعمان بن بشير'),
(SELECT id FROM public.books WHERE name = 'صحيح البخاري'),
'متفق عليه من حديث النعمان بن بشير رضي الله عنه.',
(SELECT id FROM public.explaining WHERE text = $q$يبين الحديث أن الحلال والحرام منهما ما هو واضح، وبينهما أمور مشتبهة، وأن الورع ترك المشتبهات حفظا للدين والعرض.$q$ LIMIT 1)
WHERE NOT EXISTS (
  SELECT 1 FROM public.ahadith
  WHERE book = (SELECT id FROM public.books WHERE name = 'صحيح البخاري')
    AND hadith_number = 52
);

INSERT INTO public.explaining (text)
SELECT $q$يبين الحديث فضل الفقه في الدين، وأن فهم الوحي والعمل به من علامات إرادة الخير بالعبد.$q$
WHERE NOT EXISTS (SELECT 1 FROM public.explaining WHERE text = $q$يبين الحديث فضل الفقه في الدين، وأن فهم الوحي والعمل به من علامات إرادة الخير بالعبد.$q$);

INSERT INTO public.ahadith (type, text, hadith_number, ruling, rawi, book, sanad, explaining)
SELECT 'marfu',
$q$من يرد الله به خيرا يفقهه في الدين.$q$,
71,
(SELECT id FROM public.ruling WHERE name = 'صحيح'),
(SELECT id FROM public.rawis WHERE name = 'معاوية بن أبي سفيان'),
(SELECT id FROM public.books WHERE name = 'صحيح البخاري'),
'أخرجه البخاري ومسلم بمعناه.',
(SELECT id FROM public.explaining WHERE text = $q$يبين الحديث فضل الفقه في الدين، وأن فهم الوحي والعمل به من علامات إرادة الخير بالعبد.$q$ LIMIT 1)
WHERE NOT EXISTS (
  SELECT 1 FROM public.ahadith
  WHERE book = (SELECT id FROM public.books WHERE name = 'صحيح البخاري')
    AND hadith_number = 71
);

INSERT INTO public.explaining (text)
SELECT $q$يوجه الحديث إلى نصرة المسلم لأخيه بمنعه من الظلم إن كان ظالما، وبإعانته ورفع الظلم عنه إن كان مظلوما.$q$
WHERE NOT EXISTS (SELECT 1 FROM public.explaining WHERE text = $q$يوجه الحديث إلى نصرة المسلم لأخيه بمنعه من الظلم إن كان ظالما، وبإعانته ورفع الظلم عنه إن كان مظلوما.$q$);

INSERT INTO public.ahadith (type, text, hadith_number, ruling, rawi, book, sanad, explaining)
SELECT 'marfu',
$q$انصر أخاك ظالما أو مظلوما، فقال رجل: يا رسول الله أنصره إذا كان مظلوما، أفرأيت إذا كان ظالما كيف أنصره؟ قال: تحجزه أو تمنعه من الظلم، فإن ذلك نصره.$q$,
2442,
(SELECT id FROM public.ruling WHERE name = 'صحيح'),
(SELECT id FROM public.rawis WHERE name = 'أنس بن مالك'),
(SELECT id FROM public.books WHERE name = 'صحيح البخاري'),
'أخرجه البخاري من حديث أنس بن مالك رضي الله عنه.',
(SELECT id FROM public.explaining WHERE text = $q$يوجه الحديث إلى نصرة المسلم لأخيه بمنعه من الظلم إن كان ظالما، وبإعانته ورفع الظلم عنه إن كان مظلوما.$q$ LIMIT 1)
WHERE NOT EXISTS (
  SELECT 1 FROM public.ahadith
  WHERE book = (SELECT id FROM public.books WHERE name = 'صحيح البخاري')
    AND hadith_number = 2442
);

INSERT INTO public.explaining (text)
SELECT $q$يدل الحديث على أن حسن الخلق من أعظم خصال المؤمن، وأن التفاضل بين الناس ليس بالمظاهر بل بكمال الأدب والمعاملة.$q$
WHERE NOT EXISTS (SELECT 1 FROM public.explaining WHERE text = $q$يدل الحديث على أن حسن الخلق من أعظم خصال المؤمن، وأن التفاضل بين الناس ليس بالمظاهر بل بكمال الأدب والمعاملة.$q$);

INSERT INTO public.ahadith (type, text, hadith_number, ruling, rawi, book, sanad, explaining)
SELECT 'marfu',
$q$إن من خياركم أحسنكم أخلاقا.$q$,
3559,
(SELECT id FROM public.ruling WHERE name = 'صحيح'),
(SELECT id FROM public.rawis WHERE name = 'عبد الله بن مسعود'),
(SELECT id FROM public.books WHERE name = 'صحيح البخاري'),
'أخرجه البخاري، وجاء معناه في الصحيحين.',
(SELECT id FROM public.explaining WHERE text = $q$يدل الحديث على أن حسن الخلق من أعظم خصال المؤمن، وأن التفاضل بين الناس ليس بالمظاهر بل بكمال الأدب والمعاملة.$q$ LIMIT 1)
WHERE NOT EXISTS (
  SELECT 1 FROM public.ahadith
  WHERE book = (SELECT id FROM public.books WHERE name = 'صحيح البخاري')
    AND hadith_number = 3559
);

INSERT INTO public.explaining (text)
SELECT $q$يعلم الحديث أن القوة الحقيقية هي ضبط النفس عند الغضب، لا مجرد القدرة البدنية أو الغلبة في الخصومة.$q$
WHERE NOT EXISTS (SELECT 1 FROM public.explaining WHERE text = $q$يعلم الحديث أن القوة الحقيقية هي ضبط النفس عند الغضب، لا مجرد القدرة البدنية أو الغلبة في الخصومة.$q$);

INSERT INTO public.ahadith (type, text, hadith_number, ruling, rawi, book, sanad, explaining)
SELECT 'marfu',
$q$ليس الشديد بالصرعة، إنما الشديد الذي يملك نفسه عند الغضب.$q$,
6114,
(SELECT id FROM public.ruling WHERE name = 'صحيح'),
(SELECT id FROM public.rawis WHERE name = 'أبو هريرة'),
(SELECT id FROM public.books WHERE name = 'صحيح البخاري'),
'متفق عليه من حديث أبي هريرة رضي الله عنه.',
(SELECT id FROM public.explaining WHERE text = $q$يعلم الحديث أن القوة الحقيقية هي ضبط النفس عند الغضب، لا مجرد القدرة البدنية أو الغلبة في الخصومة.$q$ LIMIT 1)
WHERE NOT EXISTS (
  SELECT 1 FROM public.ahadith
  WHERE book = (SELECT id FROM public.books WHERE name = 'صحيح البخاري')
    AND hadith_number = 6114
);

INSERT INTO public.explaining (text)
SELECT $q$ينبه الحديث إلى نعمتين يغفل عنهما كثير من الناس: الصحة والفراغ، وأنهما رأس مال للطاعة والعمل النافع.$q$
WHERE NOT EXISTS (SELECT 1 FROM public.explaining WHERE text = $q$ينبه الحديث إلى نعمتين يغفل عنهما كثير من الناس: الصحة والفراغ، وأنهما رأس مال للطاعة والعمل النافع.$q$);

INSERT INTO public.ahadith (type, text, hadith_number, ruling, rawi, book, sanad, explaining)
SELECT 'marfu',
$q$نعمتان مغبون فيهما كثير من الناس: الصحة والفراغ.$q$,
6412,
(SELECT id FROM public.ruling WHERE name = 'صحيح'),
(SELECT id FROM public.rawis WHERE name = 'عبد الله بن عباس'),
(SELECT id FROM public.books WHERE name = 'صحيح البخاري'),
'أخرجه البخاري من حديث ابن عباس رضي الله عنهما.',
(SELECT id FROM public.explaining WHERE text = $q$ينبه الحديث إلى نعمتين يغفل عنهما كثير من الناس: الصحة والفراغ، وأنهما رأس مال للطاعة والعمل النافع.$q$ LIMIT 1)
WHERE NOT EXISTS (
  SELECT 1 FROM public.ahadith
  WHERE book = (SELECT id FROM public.books WHERE name = 'صحيح البخاري')
    AND hadith_number = 6412
);

INSERT INTO public.rawis (name, gender, about)
VALUES ('عبد الله بن عباس', 'male', 'صحابي جليل، حبر الأمة وترجمان القرآن.')
ON CONFLICT (name) DO NOTHING;

INSERT INTO public.explaining (text)
SELECT $q$يجمع الحديث آدابا عظيمة: حفظ اللسان، إكرام الجار، وإكرام الضيف، وكلها من شعب الإيمان وحسن المعاشرة.$q$
WHERE NOT EXISTS (SELECT 1 FROM public.explaining WHERE text = $q$يجمع الحديث آدابا عظيمة: حفظ اللسان، إكرام الجار، وإكرام الضيف، وكلها من شعب الإيمان وحسن المعاشرة.$q$);

INSERT INTO public.ahadith (type, text, hadith_number, ruling, rawi, book, sanad, explaining)
SELECT 'marfu',
$q$من كان يؤمن بالله واليوم الآخر فليقل خيرا أو ليصمت، ومن كان يؤمن بالله واليوم الآخر فليكرم جاره، ومن كان يؤمن بالله واليوم الآخر فليكرم ضيفه.$q$,
6475,
(SELECT id FROM public.ruling WHERE name = 'صحيح'),
(SELECT id FROM public.rawis WHERE name = 'أبو هريرة'),
(SELECT id FROM public.books WHERE name = 'صحيح البخاري'),
'متفق عليه من حديث أبي هريرة رضي الله عنه.',
(SELECT id FROM public.explaining WHERE text = $q$يجمع الحديث آدابا عظيمة: حفظ اللسان، إكرام الجار، وإكرام الضيف، وكلها من شعب الإيمان وحسن المعاشرة.$q$ LIMIT 1)
WHERE NOT EXISTS (
  SELECT 1 FROM public.ahadith
  WHERE book = (SELECT id FROM public.books WHERE name = 'صحيح البخاري')
    AND hadith_number = 6475
);

-- =========================
-- EXPLAINING + AHADITH: SAHIH MUSLIM
-- =========================

INSERT INTO public.explaining (text)
SELECT $q$هذا الحديث أصل في وجوب النصيحة لله ولكتابه ولرسوله ولأئمة المسلمين وعامتهم، ومعناها إرادة الخير بصدق وإخلاص.$q$
WHERE NOT EXISTS (SELECT 1 FROM public.explaining WHERE text = $q$هذا الحديث أصل في وجوب النصيحة لله ولكتابه ولرسوله ولأئمة المسلمين وعامتهم، ومعناها إرادة الخير بصدق وإخلاص.$q$);

INSERT INTO public.ahadith (type, text, hadith_number, ruling, rawi, book, sanad, explaining)
SELECT 'marfu',
$q$الدين النصيحة، قلنا: لمن؟ قال: لله، ولكتابه، ولرسوله، ولأئمة المسلمين وعامتهم.$q$,
55,
(SELECT id FROM public.ruling WHERE name = 'صحيح'),
(SELECT id FROM public.rawis WHERE name = 'أبو رقية تميم الداري'),
(SELECT id FROM public.books WHERE name = 'صحيح مسلم'),
'أخرجه مسلم من حديث تميم الداري رضي الله عنه.',
(SELECT id FROM public.explaining WHERE text = $q$هذا الحديث أصل في وجوب النصيحة لله ولكتابه ولرسوله ولأئمة المسلمين وعامتهم، ومعناها إرادة الخير بصدق وإخلاص.$q$ LIMIT 1)
WHERE NOT EXISTS (
  SELECT 1 FROM public.ahadith
  WHERE book = (SELECT id FROM public.books WHERE name = 'صحيح مسلم')
    AND hadith_number = 55
);

INSERT INTO public.explaining (text)
SELECT $q$يرتب الحديث مراتب تغيير المنكر بحسب القدرة: اليد، ثم اللسان، ثم القلب، ويبين مسؤولية المسلم تجاه إصلاح المجتمع.$q$
WHERE NOT EXISTS (SELECT 1 FROM public.explaining WHERE text = $q$يرتب الحديث مراتب تغيير المنكر بحسب القدرة: اليد، ثم اللسان، ثم القلب، ويبين مسؤولية المسلم تجاه إصلاح المجتمع.$q$);

INSERT INTO public.ahadith (type, text, hadith_number, ruling, rawi, book, sanad, explaining)
SELECT 'marfu',
$q$من رأى منكم منكرا فليغيره بيده، فإن لم يستطع فبلسانه، فإن لم يستطع فبقلبه، وذلك أضعف الإيمان.$q$,
49,
(SELECT id FROM public.ruling WHERE name = 'صحيح'),
(SELECT id FROM public.rawis WHERE name = 'أبو سعيد الخدري'),
(SELECT id FROM public.books WHERE name = 'صحيح مسلم'),
'أخرجه مسلم من حديث أبي سعيد الخدري رضي الله عنه.',
(SELECT id FROM public.explaining WHERE text = $q$يرتب الحديث مراتب تغيير المنكر بحسب القدرة: اليد، ثم اللسان، ثم القلب، ويبين مسؤولية المسلم تجاه إصلاح المجتمع.$q$ LIMIT 1)
WHERE NOT EXISTS (
  SELECT 1 FROM public.ahadith
  WHERE book = (SELECT id FROM public.books WHERE name = 'صحيح مسلم')
    AND hadith_number = 49
);

INSERT INTO public.explaining (text)
SELECT $q$يبين الحديث فضل الطهارة والذكر والصلاة والصدقة والصبر، ويجمع أصولا عملية تقوي الإيمان وتزكي النفس.$q$
WHERE NOT EXISTS (SELECT 1 FROM public.explaining WHERE text = $q$يبين الحديث فضل الطهارة والذكر والصلاة والصدقة والصبر، ويجمع أصولا عملية تقوي الإيمان وتزكي النفس.$q$);

INSERT INTO public.ahadith (type, text, hadith_number, ruling, rawi, book, sanad, explaining)
SELECT 'marfu',
$q$الطهور شطر الإيمان، والحمد لله تملأ الميزان، وسبحان الله والحمد لله تملآن أو تملأ ما بين السماوات والأرض، والصلاة نور، والصدقة برهان، والصبر ضياء.$q$,
223,
(SELECT id FROM public.ruling WHERE name = 'صحيح'),
(SELECT id FROM public.rawis WHERE name = 'أبو مالك الأشعري'),
(SELECT id FROM public.books WHERE name = 'صحيح مسلم'),
'أخرجه مسلم من حديث أبي مالك الأشعري رضي الله عنه.',
(SELECT id FROM public.explaining WHERE text = $q$يبين الحديث فضل الطهارة والذكر والصلاة والصدقة والصبر، ويجمع أصولا عملية تقوي الإيمان وتزكي النفس.$q$ LIMIT 1)
WHERE NOT EXISTS (
  SELECT 1 FROM public.ahadith
  WHERE book = (SELECT id FROM public.books WHERE name = 'صحيح مسلم')
    AND hadith_number = 223
);

INSERT INTO public.explaining (text)
SELECT $q$يدل الحديث على عظم فضل طلب العلم، وأنه طريق موصل إلى الجنة، وأن الملائكة تكرم طالب العلم وتدعو له.$q$
WHERE NOT EXISTS (SELECT 1 FROM public.explaining WHERE text = $q$يدل الحديث على عظم فضل طلب العلم، وأنه طريق موصل إلى الجنة، وأن الملائكة تكرم طالب العلم وتدعو له.$q$);

INSERT INTO public.ahadith (type, text, hadith_number, ruling, rawi, book, sanad, explaining)
SELECT 'marfu',
$q$من سلك طريقا يلتمس فيه علما سهل الله له به طريقا إلى الجنة.$q$,
2699,
(SELECT id FROM public.ruling WHERE name = 'صحيح'),
(SELECT id FROM public.rawis WHERE name = 'أبو هريرة'),
(SELECT id FROM public.books WHERE name = 'صحيح مسلم'),
'أخرجه مسلم من حديث أبي هريرة رضي الله عنه.',
(SELECT id FROM public.explaining WHERE text = $q$يدل الحديث على عظم فضل طلب العلم، وأنه طريق موصل إلى الجنة، وأن الملائكة تكرم طالب العلم وتدعو له.$q$ LIMIT 1)
WHERE NOT EXISTS (
  SELECT 1 FROM public.ahadith
  WHERE book = (SELECT id FROM public.books WHERE name = 'صحيح مسلم')
    AND hadith_number = 2699
);

INSERT INTO public.explaining (text)
SELECT $q$ينهى الحديث عن أسباب التباغض والقطيعة كالحسد والتناجش والظلم، ويدعو إلى الأخوة الصادقة بين المسلمين.$q$
WHERE NOT EXISTS (SELECT 1 FROM public.explaining WHERE text = $q$ينهى الحديث عن أسباب التباغض والقطيعة كالحسد والتناجش والظلم، ويدعو إلى الأخوة الصادقة بين المسلمين.$q$);

INSERT INTO public.ahadith (type, text, hadith_number, ruling, rawi, book, sanad, explaining)
SELECT 'marfu',
$q$لا تحاسدوا، ولا تناجشوا، ولا تباغضوا، ولا تدابروا، وكونوا عباد الله إخوانا.$q$,
2564,
(SELECT id FROM public.ruling WHERE name = 'صحيح'),
(SELECT id FROM public.rawis WHERE name = 'أبو هريرة'),
(SELECT id FROM public.books WHERE name = 'صحيح مسلم'),
'أخرجه مسلم من حديث أبي هريرة رضي الله عنه.',
(SELECT id FROM public.explaining WHERE text = $q$ينهى الحديث عن أسباب التباغض والقطيعة كالحسد والتناجش والظلم، ويدعو إلى الأخوة الصادقة بين المسلمين.$q$ LIMIT 1)
WHERE NOT EXISTS (
  SELECT 1 FROM public.ahadith
  WHERE book = (SELECT id FROM public.books WHERE name = 'صحيح مسلم')
    AND hadith_number = 2564
);

INSERT INTO public.explaining (text)
SELECT $q$يؤكد الحديث حرمة ظلم المسلم وخذلانه واحتقاره، وأن الأخوة الإيمانية تقتضي الرحمة والنصرة وصيانة الحقوق.$q$
WHERE NOT EXISTS (SELECT 1 FROM public.explaining WHERE text = $q$يؤكد الحديث حرمة ظلم المسلم وخذلانه واحتقاره، وأن الأخوة الإيمانية تقتضي الرحمة والنصرة وصيانة الحقوق.$q$);

INSERT INTO public.ahadith (type, text, hadith_number, ruling, rawi, book, sanad, explaining)
SELECT 'marfu',
$q$المسلم أخو المسلم، لا يظلمه ولا يخذله ولا يحقره، التقوى هاهنا، بحسب امرئ من الشر أن يحقر أخاه المسلم.$q$,
2563,
(SELECT id FROM public.ruling WHERE name = 'صحيح'),
(SELECT id FROM public.rawis WHERE name = 'أبو هريرة'),
(SELECT id FROM public.books WHERE name = 'صحيح مسلم'),
'أخرجه مسلم من حديث أبي هريرة رضي الله عنه.',
(SELECT id FROM public.explaining WHERE text = $q$يؤكد الحديث حرمة ظلم المسلم وخذلانه واحتقاره، وأن الأخوة الإيمانية تقتضي الرحمة والنصرة وصيانة الحقوق.$q$ LIMIT 1)
WHERE NOT EXISTS (
  SELECT 1 FROM public.ahadith
  WHERE book = (SELECT id FROM public.books WHERE name = 'صحيح مسلم')
    AND hadith_number = 2563
);

INSERT INTO public.explaining (text)
SELECT $q$يبين الحديث أن المؤمن يتقلب بين الشكر عند النعمة والصبر عند البلاء، وكل ذلك خير له إذا صح إيمانه واحتسابه.$q$
WHERE NOT EXISTS (SELECT 1 FROM public.explaining WHERE text = $q$يبين الحديث أن المؤمن يتقلب بين الشكر عند النعمة والصبر عند البلاء، وكل ذلك خير له إذا صح إيمانه واحتسابه.$q$);

INSERT INTO public.ahadith (type, text, hadith_number, ruling, rawi, book, sanad, explaining)
SELECT 'marfu',
$q$عجبا لأمر المؤمن، إن أمره كله خير، وليس ذاك لأحد إلا للمؤمن، إن أصابته سراء شكر فكان خيرا له، وإن أصابته ضراء صبر فكان خيرا له.$q$,
2999,
(SELECT id FROM public.ruling WHERE name = 'صحيح'),
(SELECT id FROM public.rawis WHERE name = 'صهيب الرومي'),
(SELECT id FROM public.books WHERE name = 'صحيح مسلم'),
'أخرجه مسلم من حديث صهيب رضي الله عنه.',
(SELECT id FROM public.explaining WHERE text = $q$يبين الحديث أن المؤمن يتقلب بين الشكر عند النعمة والصبر عند البلاء، وكل ذلك خير له إذا صح إيمانه واحتسابه.$q$ LIMIT 1)
WHERE NOT EXISTS (
  SELECT 1 FROM public.ahadith
  WHERE book = (SELECT id FROM public.books WHERE name = 'صحيح مسلم')
    AND hadith_number = 2999
);

INSERT INTO public.rawis (name, gender, about)
VALUES ('صهيب الرومي', 'male', 'صحابي جليل، مشهور بالصبر والهجرة في سبيل الله.')
ON CONFLICT (name) DO NOTHING;

INSERT INTO public.explaining (text)
SELECT $q$فيه أن الصدقة لا تنقص المال بل تكون سببا للبركة، وأن العفو والتواضع يرفعان صاحبهما عند الله والناس.$q$
WHERE NOT EXISTS (SELECT 1 FROM public.explaining WHERE text = $q$فيه أن الصدقة لا تنقص المال بل تكون سببا للبركة، وأن العفو والتواضع يرفعان صاحبهما عند الله والناس.$q$);

INSERT INTO public.ahadith (type, text, hadith_number, ruling, rawi, book, sanad, explaining)
SELECT 'marfu',
$q$ما نقصت صدقة من مال، وما زاد الله عبدا بعفو إلا عزا، وما تواضع أحد لله إلا رفعه الله.$q$,
2588,
(SELECT id FROM public.ruling WHERE name = 'صحيح'),
(SELECT id FROM public.rawis WHERE name = 'أبو هريرة'),
(SELECT id FROM public.books WHERE name = 'صحيح مسلم'),
'أخرجه مسلم من حديث أبي هريرة رضي الله عنه.',
(SELECT id FROM public.explaining WHERE text = $q$فيه أن الصدقة لا تنقص المال بل تكون سببا للبركة، وأن العفو والتواضع يرفعان صاحبهما عند الله والناس.$q$ LIMIT 1)
WHERE NOT EXISTS (
  SELECT 1 FROM public.ahadith
  WHERE book = (SELECT id FROM public.books WHERE name = 'صحيح مسلم')
    AND hadith_number = 2588
);

INSERT INTO public.explaining (text)
SELECT $q$الدعاء يجمع الاستعاذة من أسباب الضعف والتقصير، ويعلم المسلم أن يطلب من الله القوة والنشاط والسلامة من الديون والغلبة.$q$
WHERE NOT EXISTS (SELECT 1 FROM public.explaining WHERE text = $q$الدعاء يجمع الاستعاذة من أسباب الضعف والتقصير، ويعلم المسلم أن يطلب من الله القوة والنشاط والسلامة من الديون والغلبة.$q$);

INSERT INTO public.ahadith (type, text, hadith_number, ruling, rawi, book, sanad, explaining)
SELECT 'marfu',
$q$اللهم إني أعوذ بك من العجز والكسل، والجبن والهرم والبخل، وأعوذ بك من عذاب القبر، ومن فتنة المحيا والممات.$q$,
2706,
(SELECT id FROM public.ruling WHERE name = 'صحيح'),
(SELECT id FROM public.rawis WHERE name = 'أنس بن مالك'),
(SELECT id FROM public.books WHERE name = 'صحيح مسلم'),
'أخرجه مسلم من حديث أنس بن مالك رضي الله عنه.',
(SELECT id FROM public.explaining WHERE text = $q$الدعاء يجمع الاستعاذة من أسباب الضعف والتقصير، ويعلم المسلم أن يطلب من الله القوة والنشاط والسلامة من الديون والغلبة.$q$ LIMIT 1)
WHERE NOT EXISTS (
  SELECT 1 FROM public.ahadith
  WHERE book = (SELECT id FROM public.books WHERE name = 'صحيح مسلم')
    AND hadith_number = 2706
);

INSERT INTO public.explaining (text)
SELECT $q$يبين الحديث ما يبقى نفعه للإنسان بعد موته: الصدقة الجارية والعلم المنتفع به والولد الصالح الذي يدعو له.$q$
WHERE NOT EXISTS (SELECT 1 FROM public.explaining WHERE text = $q$يبين الحديث ما يبقى نفعه للإنسان بعد موته: الصدقة الجارية والعلم المنتفع به والولد الصالح الذي يدعو له.$q$);

INSERT INTO public.ahadith (type, text, hadith_number, ruling, rawi, book, sanad, explaining)
SELECT 'marfu',
$q$إذا مات الإنسان انقطع عنه عمله إلا من ثلاثة: إلا من صدقة جارية، أو علم ينتفع به، أو ولد صالح يدعو له.$q$,
1631,
(SELECT id FROM public.ruling WHERE name = 'صحيح'),
(SELECT id FROM public.rawis WHERE name = 'أبو هريرة'),
(SELECT id FROM public.books WHERE name = 'صحيح مسلم'),
'أخرجه مسلم من حديث أبي هريرة رضي الله عنه.',
(SELECT id FROM public.explaining WHERE text = $q$يبين الحديث ما يبقى نفعه للإنسان بعد موته: الصدقة الجارية والعلم المنتفع به والولد الصالح الذي يدعو له.$q$ LIMIT 1)
WHERE NOT EXISTS (
  SELECT 1 FROM public.ahadith
  WHERE book = (SELECT id FROM public.books WHERE name = 'صحيح مسلم')
    AND hadith_number = 1631
);

-- =========================
-- TOPIC CLASSES
-- =========================
INSERT INTO public.topic_classes (topic, hadith)
SELECT t.id, h.id
FROM public.topics t
JOIN public.books b ON b.name = 'صحيح البخاري'
JOIN public.ahadith h ON h.book = b.id AND h.hadith_number = 1
WHERE t.name = 'الإخلاص والنية'
ON CONFLICT (topic, hadith) DO NOTHING;

INSERT INTO public.topic_classes (topic, hadith)
SELECT t.id, h.id
FROM public.topics t
JOIN public.books b ON b.name = 'صحيح البخاري'
JOIN public.ahadith h ON h.book = b.id AND h.hadith_number = 8
WHERE t.name = 'أركان الإسلام'
ON CONFLICT (topic, hadith) DO NOTHING;

INSERT INTO public.topic_classes (topic, hadith)
SELECT t.id, h.id
FROM public.topics t
JOIN public.books b ON b.name = 'صحيح البخاري'
JOIN public.ahadith h ON h.book = b.id AND h.hadith_number = 13
WHERE t.name = 'الأخلاق'
ON CONFLICT (topic, hadith) DO NOTHING;

INSERT INTO public.topic_classes (topic, hadith)
SELECT t.id, h.id
FROM public.topics t
JOIN public.books b ON b.name = 'صحيح البخاري'
JOIN public.ahadith h ON h.book = b.id AND h.hadith_number = 52
WHERE t.name = 'الحلال والحرام'
ON CONFLICT (topic, hadith) DO NOTHING;

INSERT INTO public.topic_classes (topic, hadith)
SELECT t.id, h.id
FROM public.topics t
JOIN public.books b ON b.name = 'صحيح البخاري'
JOIN public.ahadith h ON h.book = b.id AND h.hadith_number = 71
WHERE t.name = 'العلم'
ON CONFLICT (topic, hadith) DO NOTHING;

INSERT INTO public.topic_classes (topic, hadith)
SELECT t.id, h.id
FROM public.topics t
JOIN public.books b ON b.name = 'صحيح مسلم'
JOIN public.ahadith h ON h.book = b.id AND h.hadith_number = 49
WHERE t.name = 'الأمر بالمعروف والنهي عن المنكر'
ON CONFLICT (topic, hadith) DO NOTHING;

INSERT INTO public.topic_classes (topic, hadith)
SELECT t.id, h.id
FROM public.topics t
JOIN public.books b ON b.name = 'صحيح مسلم'
JOIN public.ahadith h ON h.book = b.id AND h.hadith_number = 223
WHERE t.name = 'الطهارة'
ON CONFLICT (topic, hadith) DO NOTHING;

INSERT INTO public.topic_classes (topic, hadith)
SELECT t.id, h.id
FROM public.topics t
JOIN public.books b ON b.name = 'صحيح مسلم'
JOIN public.ahadith h ON h.book = b.id AND h.hadith_number = 2999
WHERE t.name = 'الصبر والرضا'
ON CONFLICT (topic, hadith) DO NOTHING;

INSERT INTO public.topic_classes (topic, hadith)
SELECT t.id, h.id
FROM public.topics t
JOIN public.books b ON b.name = 'صحيح مسلم'
JOIN public.ahadith h ON h.book = b.id AND h.hadith_number = 1631
WHERE t.name = 'العمل الصالح بعد الموت'
ON CONFLICT (topic, hadith) DO NOTHING;

-- =========================
-- FAKE / WEAK POPULAR AHADITH
-- =========================
INSERT INTO public.fake_ahadith (text, ruling)
SELECT $q$اختلاف أمتي رحمة.$q$, (SELECT id FROM public.ruling WHERE name = 'لا يصح')
WHERE NOT EXISTS (SELECT 1 FROM public.fake_ahadith WHERE text = $q$اختلاف أمتي رحمة.$q$);

INSERT INTO public.fake_ahadith (text, ruling)
SELECT $q$اطلبوا العلم ولو بالصين.$q$, (SELECT id FROM public.ruling WHERE name = 'لا يصح')
WHERE NOT EXISTS (SELECT 1 FROM public.fake_ahadith WHERE text = $q$اطلبوا العلم ولو بالصين.$q$);

INSERT INTO public.fake_ahadith (text, ruling)
SELECT $q$حب الوطن من الإيمان.$q$, (SELECT id FROM public.ruling WHERE name = 'لا يصح')
WHERE NOT EXISTS (SELECT 1 FROM public.fake_ahadith WHERE text = $q$حب الوطن من الإيمان.$q$);

INSERT INTO public.fake_ahadith (text, ruling)
SELECT $q$صوموا تصحوا.$q$, (SELECT id FROM public.ruling WHERE name = 'ضعيف')
WHERE NOT EXISTS (SELECT 1 FROM public.fake_ahadith WHERE text = $q$صوموا تصحوا.$q$);

INSERT INTO public.fake_ahadith (text, ruling)
SELECT $q$الجنة تحت أقدام الأمهات، من شئن أدخلن، ومن شئن أخرجن.$q$, (SELECT id FROM public.ruling WHERE name = 'موضوع')
WHERE NOT EXISTS (SELECT 1 FROM public.fake_ahadith WHERE text = $q$الجنة تحت أقدام الأمهات، من شئن أدخلن، ومن شئن أخرجن.$q$);

INSERT INTO public.fake_ahadith (text, ruling)
SELECT $q$النظافة من الإيمان.$q$, (SELECT id FROM public.ruling WHERE name = 'لا يصح')
WHERE NOT EXISTS (SELECT 1 FROM public.fake_ahadith WHERE text = $q$النظافة من الإيمان.$q$);

INSERT INTO public.fake_ahadith (text, ruling)
SELECT $q$المعدة بيت الداء والحمية رأس الدواء.$q$, (SELECT id FROM public.ruling WHERE name = 'لا يصح')
WHERE NOT EXISTS (SELECT 1 FROM public.fake_ahadith WHERE text = $q$المعدة بيت الداء والحمية رأس الدواء.$q$);

INSERT INTO public.fake_ahadith (text, ruling)
SELECT $q$خير الأسماء ما عبد وحمد.$q$, (SELECT id FROM public.ruling WHERE name = 'لا يصح')
WHERE NOT EXISTS (SELECT 1 FROM public.fake_ahadith WHERE text = $q$خير الأسماء ما عبد وحمد.$q$);

INSERT INTO public.fake_ahadith (text, ruling)
SELECT $q$تفاءلوا بالخير تجدوه.$q$, (SELECT id FROM public.ruling WHERE name = 'لا يصح')
WHERE NOT EXISTS (SELECT 1 FROM public.fake_ahadith WHERE text = $q$تفاءلوا بالخير تجدوه.$q$);

INSERT INTO public.fake_ahadith (text, ruling)
SELECT $q$لولاك لولاك ما خلقت الأفلاك.$q$, (SELECT id FROM public.ruling WHERE name = 'موضوع')
WHERE NOT EXISTS (SELECT 1 FROM public.fake_ahadith WHERE text = $q$لولاك لولاك ما خلقت الأفلاك.$q$);

COMMIT;
