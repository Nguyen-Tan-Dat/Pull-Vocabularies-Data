DO $$
    DECLARE
v_user_id  INTEGER := 1;
        v_topic_id BIGINT;
BEGIN

        -- Bước 1: Tạo topic 'Culture' nếu chưa có
INSERT INTO public.topics (name, of_user)
VALUES ('Culture', v_user_id)
    ON CONFLICT (of_user, name) DO NOTHING;

SELECT id INTO v_topic_id
FROM   public.topics
WHERE  name    = 'Culture'
  AND  of_user = v_user_id;

RAISE NOTICE '[Topic] Culture → id = %', v_topic_id;

        -- Bước 2: Lấy tất cả vocabulary từ các topic có name LIKE 'Culture%'
        --         rồi insert vào topic 'Culture' (bỏ qua nếu đã tồn tại)
INSERT INTO public.vocabularies_topics (vocabulary, topic)
SELECT DISTINCT vt.vocabulary, v_topic_id
FROM   public.vocabularies_topics vt
           JOIN   public.topics              t  ON t.id = vt.topic
           JOIN   public.vocabularies        v  ON v.id = vt.vocabulary
WHERE  t.name    LIKE 'Culture%'
  AND  v.user_own = v_user_id
    ON CONFLICT (vocabulary, topic) DO NOTHING;

RAISE NOTICE 'XONG! Đã thêm vocabulary vào topic "Culture" (id=%)', v_topic_id;

END;
$$;

BEGIN;

TRUNCATE TABLE public.vocabularies_topics, public.vocabularies, public.topics, public.vietnamese CASCADE;

COMMIT;