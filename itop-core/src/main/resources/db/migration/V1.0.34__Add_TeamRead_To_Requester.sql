-- REQUESTER 角色增加 team:read 权限
-- 新建请求页面需要加载 IT 团队列表供用户选择
UPDATE role
SET permissions = '["request:create","request:read","request:comment","org:read","team:read"]'
WHERE role_code = 'REQUESTER';
