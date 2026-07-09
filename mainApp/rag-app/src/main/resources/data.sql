--
-- PostgreSQL database dump
--

-- Dumped from database version 16.14
-- Dumped by pg_dump version 16.2

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

--
-- Data for Name: app_user; Type: TABLE DATA; Schema: public; Owner: appuser
--

INSERT INTO public.app_user (id, password, username) VALUES (1, 'password123', 'john_doe');
INSERT INTO public.app_user (id, password, username) VALUES (2, 'password456', 'jane_smith');
INSERT INTO public.app_user (id, password, username) VALUES (3, 'password789', 'joe_bloggs');
INSERT INTO public.app_user (id, password, username) VALUES (4, '$2a$10$Cvbi/ZB/1gcnyxb5mHJYpu5fEqM9zgzlnFJp7FxE7x7HWkBC43MkW', 'user');


--
-- Data for Name: project; Type: TABLE DATA; Schema: public; Owner: appuser
--

INSERT INTO public.project (id, is_archived, is_public, name) VALUES (1, false, true, 'Book Club');


--
-- Data for Name: project_files; Type: TABLE DATA; Schema: public; Owner: appuser
--

INSERT INTO public.project_files (project_id, file_id, file_hash, index_time, file_name) VALUES (1, '9a7927f4-8813-4725-8f7d-1d5b0942d01c', '88333284c99bc1a8b6c36379258df8c4', '2026-07-09 20:01:45.098793+00', 'getWikiFiles.py');
INSERT INTO public.project_files (project_id, file_id, file_hash, index_time, file_name) VALUES (1, '49801c2d-1f31-4f56-ad29-ee814640a3d9', '372fb9da6867d17cb5eb00fda0c85211', '2026-07-09 20:15:05.291235+00', 'Michael_Ende_(author).txt');
INSERT INTO public.project_files (project_id, file_id, file_hash, index_time, file_name) VALUES (1, '089b60d5-25ae-4104-b564-c273078d815c', 'ecc19220035734eef9b176b16fd89fe8', '2026-07-09 20:16:49.537527+00', 'Robert_E._Howard.txt');
INSERT INTO public.project_files (project_id, file_id, file_hash, index_time, file_name) VALUES (1, 'fa0c0a95-bbfb-48fb-999f-cfdbc4be1fc3', 'aab7c184c829291581718ba9989d04a0', '2026-07-09 20:05:23.173109+00', 'Edward_Plunkett.txt');
INSERT INTO public.project_files (project_id, file_id, file_hash, index_time, file_name) VALUES (1, '29edcd50-8b10-4fc9-bf63-858114d81b18', 'a39b01f910d210a5d4f70aa56e25305d', '2026-07-09 20:13:58.839646+00', 'H._P._Lovecraft.txt');
INSERT INTO public.project_files (project_id, file_id, file_hash, index_time, file_name) VALUES (1, 'c3ef795f-2a2d-400b-a61d-e0559a4e5bd3', '8fdb3bf65bbe86ddb4f69720f332ba4b', '2026-07-09 20:22:00.457869+00', 'The_King_of_Elfland''s_Daughter_(Novel).txt');
INSERT INTO public.project_files (project_id, file_id, file_hash, index_time, file_name) VALUES (1, '70231048-fb7f-462a-82ff-cd73dc175ac6', '4d5f6e53fdd3375c9e96613224068508', '2026-07-09 20:06:51.894508+00', 'C._S._Lewis.txt');
INSERT INTO public.project_files (project_id, file_id, file_hash, index_time, file_name) VALUES (1, '886cb523-7bd2-42d4-8439-4839ada96071', '65261a6a69d6b2c685b93115110a0ccc', '2026-07-09 20:21:45.944139+00', 'The_Hobbit.txt');
INSERT INTO public.project_files (project_id, file_id, file_hash, index_time, file_name) VALUES (1, '1fb50962-c863-4469-ac84-b82d2e00a254', 'b736a61de1f15123d48c9bc64c2f51af', '2026-07-09 20:24:03.156522+00', 'The_Worm_Ouroboros.txt');
INSERT INTO public.project_files (project_id, file_id, file_hash, index_time, file_name) VALUES (1, 'cd640449-3493-4f16-bde5-62974a417d76', '9a7e56fa580b875a45f123408461733e', '2026-07-09 20:02:43.097744+00', 'Cthulhu_Mythos.txt');
INSERT INTO public.project_files (project_id, file_id, file_hash, index_time, file_name) VALUES (1, '25c28c67-5ca8-4afb-82bb-01bd666aef12', '2a1b4f8047b068237184ab0bdca72f53', '2026-07-09 20:14:07.747686+00', 'Lud-in-the-Mist.txt');
INSERT INTO public.project_files (project_id, file_id, file_hash, index_time, file_name) VALUES (1, '04b60400-8e28-4607-9826-53f940f28926', '0e7cb1b60d04350841ba1b0dd4a03194', '2026-07-09 20:03:00.640713+00', 'Hope_Mirrlees.txt');
INSERT INTO public.project_files (project_id, file_id, file_hash, index_time, file_name) VALUES (1, '693cb12a-203f-4d3b-ba49-1e8a0da72314', 'adbb3db2c2f28ca00265a67fbb3f5d69', '2026-07-09 20:20:02.475769+00', 'The_Chronicles_of_Narnia.txt');
INSERT INTO public.project_files (project_id, file_id, file_hash, index_time, file_name) VALUES (1, '4e93b10c-0b2c-42a3-86a6-f2a5290cdb47', 'a493769aac2688a043cc3ecd95c06056', '2026-07-09 20:02:11.569894+00', 'E._R._Eddison.txt');
INSERT INTO public.project_files (project_id, file_id, file_hash, index_time, file_name) VALUES (1, '5413364a-7700-40b7-b4c1-a85bad45d216', '9b1da6acc13677b8c36295dc538c5e1d', '2026-07-09 20:10:11.91155+00', 'Lewis_Carroll.txt');
INSERT INTO public.project_files (project_id, file_id, file_hash, index_time, file_name) VALUES (1, '308860d7-a580-4ee8-b69e-409eae77fd62', 'ca765c5797b5bfcb752da27ed3ddecd7', '2026-07-09 20:11:47.223083+00', 'Conan_the_Barbarian.txt');
INSERT INTO public.project_files (project_id, file_id, file_hash, index_time, file_name) VALUES (1, 'd8349508-05ce-4940-8dae-46791805fd1e', 'f4fcf31f27e12ef91a43a11d433ea051', '2026-07-09 20:20:22.071027+00', 'The_Color_of_Magic.txt');
INSERT INTO public.project_files (project_id, file_id, file_hash, index_time, file_name) VALUES (1, 'b72097b5-74d4-470b-b5a9-bb502b2ccffc', '44e3a2a4c4419a552f2efb39f492df28', '2026-07-09 20:18:04.695292+00', 'test.html');
INSERT INTO public.project_files (project_id, file_id, file_hash, index_time, file_name) VALUES (1, '1a6088a2-cc5c-48c4-83e3-d9a9a63df715', '1ae6437976a445f1bbe6a611a8fc3e0c', '2026-07-09 20:18:04.371523+00', 'Terry_Pratchett.txt');
INSERT INTO public.project_files (project_id, file_id, file_hash, index_time, file_name) VALUES (1, '488f7c45-e516-40f9-abd3-1553c497ba24', '9fc37c4bbe6f8b5943e15d1400da34b0', '2026-07-09 20:23:09.998855+00', 'The_Princess_Bride_(novel).txt');
INSERT INTO public.project_files (project_id, file_id, file_hash, index_time, file_name) VALUES (1, '857aa83e-3c2f-4921-92d6-b06379592ce0', '41e22fc23a3437a65e9defe731f3e2ae', '2026-07-09 20:24:57.012804+00', 'William_Goldman.txt');
INSERT INTO public.project_files (project_id, file_id, file_hash, index_time, file_name) VALUES (1, '7b172dba-8c4c-4e71-b536-f1befe59fb01', 'f818df9aa2e67d99cdbabb5d6bc4ec08', '2026-07-09 20:04:08.454957+00', 'Alice''s_Adventures_in_Wonderland.txt');
INSERT INTO public.project_files (project_id, file_id, file_hash, index_time, file_name) VALUES (1, 'f862fd7f-343d-4c39-8d31-9ffddcb93e12', '263639553ec25e5cdca511b71b7c5134', '2026-07-09 20:22:33.453702+00', 'The_Neverending_Story.txt');
INSERT INTO public.project_files (project_id, file_id, file_hash, index_time, file_name) VALUES (1, '67095cad-d201-484b-8429-466eec639070', '66f6448db3d364cb7a7f00a017f98da2', '2026-07-09 20:08:33.756989+00', 'J._R._R._Tolkien.txt');


--
-- Data for Name: project_user_access; Type: TABLE DATA; Schema: public; Owner: appuser
--



--
-- Data for Name: project_user_admin; Type: TABLE DATA; Schema: public; Owner: appuser
--

INSERT INTO public.project_user_admin (project_id, user_id) VALUES (1, 4);


--
-- Data for Name: question; Type: TABLE DATA; Schema: public; Owner: appuser
--



--
-- Data for Name: question_sources; Type: TABLE DATA; Schema: public; Owner: appuser
--



--
-- Name: app_user_id_seq; Type: SEQUENCE SET; Schema: public; Owner: appuser
--

SELECT pg_catalog.setval('public.app_user_id_seq', 4, true);


--
-- Name: project_id_seq; Type: SEQUENCE SET; Schema: public; Owner: appuser
--

SELECT pg_catalog.setval('public.project_id_seq', 1, true);


--
-- Name: question_id_seq; Type: SEQUENCE SET; Schema: public; Owner: appuser
--

SELECT pg_catalog.setval('public.question_id_seq', 1, false);


--
-- PostgreSQL database dump complete
--

