--
-- PostgreSQL database dump
--

\restrict V0eb4JLAmoDmFm1D3Qzzlf7NOvw7SxhuWXbdgP5JBTu66zMdSPabVITM4ajiuAL

-- Dumped from database version 18.1
-- Dumped by pg_dump version 18.1

-- Started on 2026-01-21 20:20:50

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET transaction_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- TOC entry 225 (class 1259 OID 16443)
-- Name: parking_orders; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.parking_orders (
    id integer NOT NULL,
    "User_ID" integer NOT NULL,
    spot_number integer NOT NULL,
    owner_phone character varying(11) NOT NULL,
    car_number character varying(8) NOT NULL,
    status character varying(10) NOT NULL
);


ALTER TABLE public.parking_orders OWNER TO postgres;

--
-- TOC entry 224 (class 1259 OID 16442)
-- Name: parking_orders_User_ID_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public."parking_orders_User_ID_seq"
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public."parking_orders_User_ID_seq" OWNER TO postgres;

--
-- TOC entry 5041 (class 0 OID 0)
-- Dependencies: 224
-- Name: parking_orders_User_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public."parking_orders_User_ID_seq" OWNED BY public.parking_orders."User_ID";


--
-- TOC entry 223 (class 1259 OID 16441)
-- Name: parking_orders_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.parking_orders_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.parking_orders_id_seq OWNER TO postgres;

--
-- TOC entry 5042 (class 0 OID 0)
-- Dependencies: 223
-- Name: parking_orders_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.parking_orders_id_seq OWNED BY public.parking_orders.id;


--
-- TOC entry 222 (class 1259 OID 16410)
-- Name: parking_spots; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.parking_spots (
    spot_number integer NOT NULL,
    "Price" integer,
    is_occupied boolean DEFAULT false NOT NULL,
    CONSTRAINT spot_number CHECK (((spot_number >= 1) AND (spot_number <= 100))),
    CONSTRAINT spot_number_max_100 CHECK (((spot_number >= 1) AND (spot_number <= 100)))
);


ALTER TABLE public.parking_spots OWNER TO postgres;

--
-- TOC entry 221 (class 1259 OID 16409)
-- Name: parking_spots_spot_number_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.parking_spots_spot_number_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.parking_spots_spot_number_seq OWNER TO postgres;

--
-- TOC entry 5043 (class 0 OID 0)
-- Dependencies: 221
-- Name: parking_spots_spot_number_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.parking_spots_spot_number_seq OWNED BY public.parking_spots.spot_number;


--
-- TOC entry 220 (class 1259 OID 16400)
-- Name: users; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.users (
    "User_ID" integer NOT NULL,
    username character varying,
    password character varying
);


ALTER TABLE public.users OWNER TO postgres;

--
-- TOC entry 219 (class 1259 OID 16399)
-- Name: users_User_ID_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public."users_User_ID_seq"
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public."users_User_ID_seq" OWNER TO postgres;

--
-- TOC entry 5044 (class 0 OID 0)
-- Dependencies: 219
-- Name: users_User_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public."users_User_ID_seq" OWNED BY public.users."User_ID";


--
-- TOC entry 4870 (class 2604 OID 16446)
-- Name: parking_orders id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.parking_orders ALTER COLUMN id SET DEFAULT nextval('public.parking_orders_id_seq'::regclass);


--
-- TOC entry 4871 (class 2604 OID 16447)
-- Name: parking_orders User_ID; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.parking_orders ALTER COLUMN "User_ID" SET DEFAULT nextval('public."parking_orders_User_ID_seq"'::regclass);


--
-- TOC entry 4868 (class 2604 OID 16413)
-- Name: parking_spots spot_number; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.parking_spots ALTER COLUMN spot_number SET DEFAULT nextval('public.parking_spots_spot_number_seq'::regclass);


--
-- TOC entry 4867 (class 2604 OID 16403)
-- Name: users User_ID; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.users ALTER COLUMN "User_ID" SET DEFAULT nextval('public."users_User_ID_seq"'::regclass);


--
-- TOC entry 5035 (class 0 OID 16443)
-- Dependencies: 225
-- Data for Name: parking_orders; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.parking_orders (id, "User_ID", spot_number, owner_phone, car_number, status) FROM stdin;
2	1	100	87777777777	111AAA11	ACTIVE
3	1	67	81111111111	000SSS01	ACTIVE
\.


--
-- TOC entry 5032 (class 0 OID 16410)
-- Dependencies: 222
-- Data for Name: parking_spots; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.parking_spots (spot_number, "Price", is_occupied) FROM stdin;
1	5000	f
2	5000	f
3	5000	f
4	5000	f
5	5000	f
6	5000	f
7	5000	f
8	5000	f
9	5000	f
10	5000	f
11	5000	f
12	5000	f
13	5000	f
14	5000	f
15	5000	f
16	5000	f
17	5000	f
18	5000	f
19	5000	f
20	5000	f
21	5000	f
22	5000	f
23	5000	f
24	5000	f
25	5000	f
26	5000	f
27	5000	f
28	5000	f
29	5000	f
30	5000	f
31	5000	f
32	5000	f
33	5000	f
34	5000	f
35	5000	f
36	5000	f
37	5000	f
38	5000	f
39	5000	f
40	5000	f
41	5000	f
42	5000	f
43	5000	f
44	5000	f
45	5000	f
46	5000	f
47	5000	f
48	5000	f
49	5000	f
50	5000	f
51	5000	f
52	5000	f
53	5000	f
54	5000	f
55	5000	f
56	5000	f
57	5000	f
58	5000	f
59	5000	f
60	5000	f
61	5000	f
62	5000	f
63	5000	f
64	5000	f
65	5000	f
66	5000	f
67	5000	f
68	5000	f
69	5000	f
70	5000	f
71	5000	f
72	5000	f
73	5000	f
74	5000	f
75	5000	f
76	5000	f
77	5000	f
78	5000	f
79	5000	f
80	5000	f
81	5000	f
82	5000	f
83	5000	f
84	5000	f
85	5000	f
86	5000	f
87	5000	f
88	5000	f
89	5000	f
90	5000	f
91	5000	f
92	5000	f
93	5000	f
94	5000	f
95	5000	f
96	5000	f
97	5000	f
98	5000	f
99	5000	f
100	5000	f
\.


--
-- TOC entry 5030 (class 0 OID 16400)
-- Dependencies: 220
-- Data for Name: users; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.users ("User_ID", username, password) FROM stdin;
1	Sultan	Ali
2	Sultan	Ali
\.


--
-- TOC entry 5045 (class 0 OID 0)
-- Dependencies: 224
-- Name: parking_orders_User_ID_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public."parking_orders_User_ID_seq"', 1, false);


--
-- TOC entry 5046 (class 0 OID 0)
-- Dependencies: 223
-- Name: parking_orders_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.parking_orders_id_seq', 3, true);


--
-- TOC entry 5047 (class 0 OID 0)
-- Dependencies: 221
-- Name: parking_spots_spot_number_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.parking_spots_spot_number_seq', 1, false);


--
-- TOC entry 5048 (class 0 OID 0)
-- Dependencies: 219
-- Name: users_User_ID_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public."users_User_ID_seq"', 2, true);


-- Completed on 2026-01-21 20:20:50

--
-- PostgreSQL database dump complete
--

\unrestrict V0eb4JLAmoDmFm1D3Qzzlf7NOvw7SxhuWXbdgP5JBTu66zMdSPabVITM4ajiuAL

