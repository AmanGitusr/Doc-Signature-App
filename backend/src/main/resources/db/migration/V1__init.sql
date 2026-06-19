create table users (
    id bigint primary key auto_increment,
    full_name varchar(120) not null,
    email varchar(180) not null unique,
    password_hash varchar(255) not null,
    role varchar(30) not null,
    created_at timestamp not null,
    updated_at timestamp not null
);

create table documents (
    id bigint primary key auto_increment,
    owner_id bigint not null,
    title varchar(255) not null,
    description varchar(1000) null,
    original_filename varchar(255) not null,
    stored_filename varchar(255) not null,
    content_type varchar(120) not null,
    file_size bigint not null,
    status varchar(40) not null,
    created_at timestamp not null,
    updated_at timestamp not null,
    finalized_at timestamp null,
    constraint fk_documents_owner foreign key (owner_id) references users(id)
);

create table signature_requests (
    id bigint primary key auto_increment,
    document_id bigint not null unique,
    signer_name varchar(120) not null,
    signer_email varchar(180) not null,
    token varchar(80) not null unique,
    page_number int not null,
    x_position double not null,
    y_position double not null,
    status varchar(40) not null,
    signature_text varchar(255) null,
    rejection_reason varchar(500) null,
    requested_at timestamp not null,
    signed_at timestamp null,
    rejected_at timestamp null,
    constraint fk_signature_requests_document foreign key (document_id) references documents(id)
);

create table audit_events (
    id bigint primary key auto_increment,
    document_id bigint null,
    actor_user_id bigint null,
    actor_type varchar(40) not null,
    action varchar(80) not null,
    details varchar(1000) not null,
    ip_address varchar(80) null,
    created_at timestamp not null,
    constraint fk_audit_document foreign key (document_id) references documents(id),
    constraint fk_audit_actor foreign key (actor_user_id) references users(id)
);
