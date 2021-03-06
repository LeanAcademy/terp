--import.sql

--insert user data �� G��Y���� �� "�`Y[�c�-#o�e(�"
insert into terp.grup(grup_adi, durum)
values('Administrators', 0);

insert into terp.kullanici(isim, sifre, kullanici_adi, 
                            durum, tip, grup_ref)
values('Administrator', '�� G��Y���� ��', 'Admin', 0, 1, 1);

--insert core plugins data
insert into terp.eklenti(ref_num, eklenti_adi, tip, sinif_adi)
values(1, 'terp.core-1.0-SNAPSHOT', 0, 'com.terp.core.plugin.Plugin');

--insert comapany info
insert into terp.firma(firma_unv, firma_adi, durum, vergi_no, vergi_dairesi, 
aciklama, merkez_adresi, merkez_sehir, merkez_ilce, merkez_ulke, email)
values('Lean danışmanlık', 'lean danışmanlık sanayi ve ticaret ltd. şti.', 0, 
    '6086525750', 'ÇERKEZKÖY', null, 'inönü mah. 77.sk no: 6-1/B2/D38', 
    'TEKİRDAĞ', 'KAPAKLI', 'TÜRKİYE', 'dal.cevdet@gmail.com');

--insert menu
insert into terp.menu(ref_num, menu_kodu, menu_aciklama, menu_tipi, ust_menu, 
durum, program, eklenti, ekl_ref_num)
values(1,'SYS01', 'Sistem yönetimi', 0, 0, 0, null, 0,null),
        (2,'SYS02', 'Eklenti yükleme', 1, 1, 0, 'PluginInstaller', 0, null ),
        (3,'SYS03', 'Yetki yönetimi', 1, 1, 0, null, 0, null),
        (4,'SYS04', 'Parametre yönetimi', 0, 0, 0, null, 0, null),
        (5,'FRMYS01', 'Firma yönetimi', 0, 0, 0, null, 0, null),
        (6,'FRMYS02', 'Firma tanımı', 1, 5, 0, 'CompanyForm', 1, 1);

--insert malzeme
insert into terp.malzeme(ref_num, mlz_kodu, mlz_tanimi, mlz_birimi, EKL_KUL_ID)
values(1, '1234', 'DENEME MLZ', 'AD', 1);