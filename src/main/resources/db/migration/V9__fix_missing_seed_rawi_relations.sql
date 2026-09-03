UPDATE public.ahadith h
SET rawi = r.id
FROM public.books b, public.rawis r
WHERE h.book = b.id
  AND b.name = 'صحيح البخاري'
  AND h.hadith_number = 6412
  AND r.name = 'عبد الله بن عباس'
  AND h.rawi IS NULL;

UPDATE public.ahadith h
SET rawi = r.id
FROM public.books b, public.rawis r
WHERE h.book = b.id
  AND b.name = 'صحيح مسلم'
  AND h.hadith_number = 2999
  AND r.name = 'صهيب الرومي'
  AND h.rawi IS NULL;
