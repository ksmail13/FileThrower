
CREATE TABLE InfoBase (
                infoId CHAR NOT NULL primary key,
                name VARCHAR NOT NULL,
);


CREATE TABLE AccountInfo (
                accountId CHAR(10) NOT NULL primary key references infobase(infoid) ON DELETE CASCADE ON UPDATE CASCADE,
                id VARCHAR NOT NULL,
                password VARCHAR NOT NULL,
                email VARCHAR NOT NULL
);


CREATE TABLE GroupInfo (
                groupId CHAR(10) NOT NULL primary key references infobase(infoid) ON DELETE CASCADE ON UPDATE CASCADE,
                groupMemberList ARRAY NOT NULL
);


CREATE TABLE GroupMemberInfo (
                groupId CHAR(10) NOT NULL references infobase(infoid) ON DELETE CASCADE ON UPDATE CASCADE,
                accountId CHAR(10) NOT NULL references infobase(infoid) ON DELETE CASCADE ON UPDATE CASCADE,
                permission CHAR NOT NULL,
		accept char NOT NULL ’N’
);


CREATE TABLE FileInfo (
                fileId CHAR(10) NOT NULL primary key references infobase(infoid) ON DELETE CASCADE ON UPDATE CASCADE,
                filsSize INTEGER NOT NULL,
                groupId CHAR(10) NOT NULL references infobase(infoid) ON DELETE CASCADE ON UPDATE CASCADE
);


CREATE TABLE History (
	historyId serial primary key,
	groupId CHAR(10) NOT NULL references groupinfo(groupid) ON DELETE CASCADE ON UPDATE CASCADE,
	accountId CHAR(10) NOT NULL references accountinfo(accountid) ON DELETE CASCADE ON UPDATE CASCADE,
	fileId CHAR(10) NOT NULL references fileinfo(fileid) ON DELETE CASCADE ON UPDATE CASCADE,
	jobType varchar,
	time timestamp NOT NULL
);

Create or replace function addmember_stamp() returns trigger as $am_stp$
begin
	Insert into history (groupid, accountid, fileid, jobtype, time) 
	values (NEW.groupId, NEW.accountid, '', 'adduser', now());
	return NEW;
END;
$am_stp$ LANGUAGE plpgsql;

CREATE TRIGGER addmember_history
AFTER INSERT on groupmemberinfo
FOR EACH ROW EXECUTE PROCEDURE addmember_stamp();

Create or replace function addfile_stamp() returns trigger as $af_stp$
begin
	Insert into history (groupid, accountid, fileid, jobtype, time) 
	values (NEW.groupId, NEW.accountid, NEW.fileid, 'addfile', now());
	return NEW;
END;
$af_stp$ LANGUAGE plpgsql;

CREATE TRIGGER addfile_history
AFTER INSERT on fileinfo
FOR EACH ROW EXECUTE PROCEDURE addfile_stamp();


create or replace function accountgroupinfo(char(10))
	returns table(groupid char(10), groupcomment varchar, groupname varchar, masterid char(10), mastername varchar)
 as $$
select
	g.groupid,
	groupcomment,
	groupname,
	masterid,
	mastername
from
	(select 
		groupid,
		groupcomment,
		groupname,
		masterid,
		i.name as mastername
	from
		(select 
			gi.groupid, 
			groupcomment, 
			i.name as groupname,
			masterid
		from
			(select 
				g.groupid,
				g.comment as groupcomment,
				accountid as masterid
			from
				groupinfo as g,
				groupmemberinfo as gmi
			where	
				g.groupid=gmi.groupid
				and gmi.permission='M') as gi,
			infobase as i
		where
			gi.groupid = i.infoid) as g
	left join
		infobase as i
	on g.masterid = i.infoid) as g
,
	groupmemberinfo as gmi
where
	g.groupid = gmi.groupid
	and
	gmi.accountid=$1;
$$
language sql;