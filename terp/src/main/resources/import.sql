--import.sql

--insert user data �� G��Y���� �� "�`Y[�c�-#o�e(�"
insert into terp.grup(grup_adi, durum)
values('Administrators', 0);

insert into terp.kullanici(isim, sifre, kullanici_adi, 
                            durum, tip, grup_ref)
values('Administrator', '�� G��Y���� ��', 'Admin', 0, 1, 1);

--insert core plugins data
insert into terp.eklenti(eklenti_adi, tip)
values('terp.core', 0);

--insert comapany info
insert into terp.firma(firma_unv, firma_adi, durum)
values('Lean danışmanlık', 'lean danışmanlık sanayi ve ticaret ltd. şti.', 0);

--insert menu
insert into terp.menu(ref_num, menu_kodu, menu_aciklama, menu_tipi, ust_menu, durum)
values(1,'CH01', 'Cari hesap yönetimi', 0, 0, 0), 
        (2,'MM01', 'Malzeme yönetimi', 0, 0, 0),
        (3,'MRP01', 'Malzeme ihtiyaç planlaması', 0, 0, 0),
        (4,'URT01', 'Üretim yönetimi', 0, 0, 0),
        (5,'SYS01', 'Sistem yönetimi', 0, 0, 0),
        (6,'SYS02', 'Menü yönetimi', 1, 5, 0),
        (7,'SYS03', 'Yetki yönetimi', 1, 5, 0);