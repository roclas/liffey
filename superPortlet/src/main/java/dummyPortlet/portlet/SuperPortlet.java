package dummyPortlet.portlet;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import javax.portlet.Portlet;
import javax.portlet.PortletException;

import org.osgi.service.component.annotations.Component;

import com.liferay.document.library.kernel.model.DLFolder;
import com.liferay.document.library.kernel.service.DLFolderLocalServiceUtil;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.model.ResourceAction;
import com.liferay.portal.kernel.model.ResourceConstants;
import com.liferay.portal.kernel.model.ResourcePermission;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCPortlet;
import com.liferay.portal.kernel.service.ResourceActionLocalServiceUtil;
import com.liferay.portal.kernel.service.ResourcePermissionLocalServiceUtil;
import com.liferay.portal.kernel.service.RoleLocalServiceUtil;

import dummyPortlet.constants.DummyPortletKeys;

/**
 * @author carlos
 */
@Component(
	immediate = true,
	property = {
		"com.liferay.portlet.display-category=category.sample",
		"com.liferay.portlet.instanceable=true",
		"javax.portlet.display-name=dummyPortlet Portlet",
		"javax.portlet.init-param.template-path=/",
		"javax.portlet.init-param.view-template=/view.jsp",
		"javax.portlet.name=" + DummyPortletKeys.Dummy,
		"javax.portlet.resource-bundle=content.Language",
		"javax.portlet.security-role-ref=power-user,user"
	},
	service = Portlet.class
)
public class SuperPortlet extends MVCPortlet {
	
	long groupId=20143;
	long parentFolderId=35128;
	long companyId=20116;

	/**
	 * This method is used to trigger one of the methods
	 */
	@Override
	public void init() throws PortletException {
		super.init();
		
		List<DLFolder> folders = getFoldersRecursively(groupId, parentFolderId);
		List<ResourcePermission> resourcePermissions = ResourcePermissionLocalServiceUtil.getResourcePermissions(companyId,DLFolder.class.getName(),ResourceConstants.SCOPE_INDIVIDUAL,String.valueOf(parentFolderId));
		folders.forEach(f->{
				try {
					System.out.println(String.format("\n\n\n%s (%s)",f.getPath(),f.getFolderId()));
					//printFolderPermissions(f);
					setFolderPermissions(f,resourcePermissions,3);
				} catch (PortalException e) {
					e.printStackTrace();
				}
		});
	}


	/**
	 * Prints the permissions of a folder
	 * @param f
	 */
	private void printFolderPermissions(DLFolder f) {
		List<ResourcePermission> resourcePermissions = ResourcePermissionLocalServiceUtil.getResourcePermissions(companyId,DLFolder.class.getName(),ResourceConstants.SCOPE_INDIVIDUAL,String.valueOf(f.getFolderId()));
		List<ResourceAction> resourceActions = ResourceActionLocalServiceUtil.getResourceActions(DLFolder.class.getName())
		.stream().sorted((r1,r2)->Integer.parseInt(""+(r2.getBitwiseValue()-r1.getBitwiseValue()))).collect(Collectors.toList());

		resourcePermissions.forEach(p->{
			try {
				System.out.print(RoleLocalServiceUtil.getRole(p.getRoleId()).getName());
			} catch (PortalException e) {
				e.printStackTrace();
			}
			ArrayList<Boolean> rlist= new ArrayList<Boolean>();
			ArrayList<String> nlist= new ArrayList<String>();
			Iterator<ResourceAction> it = resourceActions.iterator();
			long aids=p.getActionIds();
			while(it.hasNext()){
				ResourceAction current = it.next();
				if(aids>=current.getBitwiseValue()){
					aids-=current.getBitwiseValue();
					rlist.add(true);
				}else rlist.add(false);
				nlist.add(current.getActionId());
			}
			for(int i=0;i<rlist.size();i++){
				System.out.print(String.format(",%s=%s", nlist.get(i),rlist.get(i)));
			}
			System.out.println("");
		});
	}

	/**
	 * Propagates the permissions of a folder to the other folders
	 * @param f
	 * @throws PortalException 
	 */
	private void setFolderPermissions(DLFolder f,List<ResourcePermission> newResourcePermissions,int attempts) throws PortalException {
		if (attempts==0)return;
		System.out.println(String.format("setting roles for %s", f.getPath()));
		newResourcePermissions.forEach(p->{
		long actIds = p.getActionIds();
			try {
				ResourcePermission currentPermission = ResourcePermissionLocalServiceUtil.getResourcePermission(
						companyId,
						DLFolder.class.getName(),
						ResourceConstants.SCOPE_INDIVIDUAL,
						""+f.getPrimaryKey(),
						p.getRoleId());
				currentPermission.setActionIds(actIds);
				System.out.print(RoleLocalServiceUtil.getRole(p.getRoleId()).getName());
				System.out.println(String.format(": before=%s, now=%s", currentPermission.getActionIds(),actIds));
				ResourcePermissionLocalServiceUtil.updateResourcePermission(currentPermission);
			} catch (SystemException | PortalException e) {
				try {
					System.out.println(String.format("exception for role %s:", RoleLocalServiceUtil.getRole(p.getRoleId()).getName()));
					ResourcePermission currentPermission = ResourcePermissionLocalServiceUtil.getResourcePermission(
						companyId,
						DLFolder.class.getName(),
						ResourceConstants.SCOPE_INDIVIDUAL,
						""+f.getPrimaryKey(),
						p.getRoleId());
					System.out.println(String.format("old=%s, new=%s", currentPermission.getActionIds(),actIds));
					Thread.sleep(3000);
					setFolderPermissions(f, newResourcePermissions, attempts-1);
				}catch(Exception e2){
					e.printStackTrace();
				}

			}
		});
	}

	/**
	 * Returns a list of all sub-folders of a given one
	 * @param groupId
	 * @param parentFolderId
	 * @return
	 */
	private List<DLFolder> getFoldersRecursively(long groupId, long parentFolderId){
		List<DLFolder> folders = DLFolderLocalServiceUtil.getFolders(groupId, parentFolderId);
		if(folders.isEmpty()) return folders;
		List<DLFolder> result= new ArrayList<DLFolder>();
		folders.forEach(f->{
			result.add(f);
			getFoldersRecursively(groupId, f.getFolderId()).forEach(f2->result.add(f2));
		});
		return result;
	}

}