/*global cnrutils,search,status,args,_,logger,jsonUtils,model,requestbody */
function main() {
  "use strict";
  var json = jsonUtils.toObject(requestbody.content),
    applicationSource = search.findNode(json.applicationSourceId),
    userId = applicationSource.properties["jconon_application:user"],
    groupRdP = json.groupRdP,
    j = 0,
    child;

  applicationSource.properties["jconon_application:stato_domanda"] = "P";
  applicationSource.properties["jconon_application:data_ultimo_invio"] = applicationSource.properties["jconon_application:data_domanda"];
  applicationSource.properties["jconon_application:data_domanda"] = null;
  applicationSource.properties["jconon_application:esclusione_rinuncia"] = null;
  applicationSource.save();
  applicationSource.removePermission("Contributor", groupRdP);
  applicationSource.removePermission("Consumer", userId);
  applicationSource.setPermission("Contributor", userId);
  for (j = 0; j < applicationSource.children.length; j++) {
    child = applicationSource.children[j];
    if (String(child.getTypeShort()) !==  "jconon_attachment:application" &&
        String(child.getOwner()) !== userId) {
      child.setOwner(userId);
    }
    if (String(child.getTypeShort()) ===  "jconon_esclusione:attachment") {
      child.remove();
    }

  }
  model.esito = true;
}
main();