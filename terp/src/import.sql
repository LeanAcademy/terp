--import.sql

--insert user data
insert into terp.grup(grup_adi, durum)
values('Administrators', 0);

insert into terp.kullanici(isim, sifre, kullanici_adi, 
                            durum, tip, grup_ref)
values('Administrator', '"�`Y[�c�-#o�e(�', 'Admin', 0, 1, 1);

--insert core plugins data
insert into terp.eklenti(eklenti_adi, tip)
values('terp.core', 0);

--insert value into company table
insert into terp.firma(firma_unv, firma_adi,durum)
values('Test', 'Test company', 0);