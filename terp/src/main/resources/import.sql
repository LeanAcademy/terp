--import.sql

--insert user data �� G��Y���� �� "�`Y[�c�-#o�e(�"
insert into terp.grup(grup_adi, durum)
values('Administrators', 0);

insert into terp.kullanici(isim, sifre, kullanici_adi, 
                            durum, tip, grup_ref)
values('Administrator', '�� G��Y���� ��', 'Admin', 0, 1, 1);

--insert core plugins data
insert into terp.eklenti(eklenti_adi, tip, sinif_adi)
values('terp.core-1.0-SNAPSHOT', 0, 'com.terp.core.plugin.Plugin');

--insert comapany info
insert into terp.firma(firma_unv, firma_adi, durum)
values('Lean danışmanlık', 'lean danışmanlık sanayi ve ticaret ltd. şti.', 0);

--insert menu
insert into terp.menu(ref_num, menu_kodu, menu_aciklama, menu_tipi, ust_menu, durum, program, eklenti)
values(1,'SYS01', 'Sistem yönetimi', 0, 0, 0, null, 0),
        (2,'SYS02', 'Eklenti yükleme', 1, 1, 0, 'PluginInstaller',0 ),
        (3,'SYS03', 'Yetki yönetimi', 1, 1, 0, null, 0),
        (4,'SYS04', 'Parametre yönetimi', 0, 0, 0, null,0);