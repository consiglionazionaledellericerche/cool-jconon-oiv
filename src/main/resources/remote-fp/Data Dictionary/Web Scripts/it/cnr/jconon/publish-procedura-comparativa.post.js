/*global jsonUtils,requestbody,logger,status,groupAuthority,model,search,args */
/**
 * Post children
 */
var json = jsonUtils.toObject(requestbody.content),
  nodeRef = json.nodeRef,
  userid = json.userid,
  publish = json.publish,
  esito = json.esito,
  CONSUMER = "Consumer",
  COORDINATOR = "Coordinator",
  EDITOR = "Editor",
  CONTRIBUTOR = "Contributor",
  creator,
  i = 0;

if (nodeRef === null || userid === null) {
  status.setCode(status.STATUS_BAD_REQUEST, "You must specify nodeRef and userid");
  model.esito = false;
} else {
  var node = search.findNode(nodeRef), child, creator = node.properties['cm:creator'];
  for (i = 0; i < node.children.length; i++) {
    child = node.children[i];
    child.setOwner(userid);
    if (publish) {
      if (String(child.type) !== "{http://www.cnr.it/model/jconon_attachment/cmis}call_fp_esito_elenco_codici_iscrizione") {
        child.setPermission(CONSUMER, "GROUP_EVERYONE");
      }
      child.setInheritsPermissions(false);
    } else {
      child.removePermission(CONSUMER, "GROUP_EVERYONE");
      child.setInheritsPermissions(true);      
    }
  }
  if (publish) {
    node.setPermission(CONSUMER, "GROUP_EVERYONE");
    node.removePermission(COORDINATOR, "GROUP_CONCORSI");    
    if (!esito) {
      node.setPermission(EDITOR, creator);
      node.setPermission(CONTRIBUTOR, creator);
      node.setPermission(EDITOR, "GROUP_CONCORSI");
      node.setPermission(CONTRIBUTOR, "GROUP_CONCORSI");      
    }
  } else {
    node.removePermission(CONSUMER, "GROUP_EVERYONE");
    node.setPermission(COORDINATOR, "GROUP_CONCORSI");
  }
  node.setOwner(userid);
}