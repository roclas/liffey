package dummyPortlet.portlet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.portlet.Portlet;
import javax.portlet.PortletException;

import org.osgi.service.component.annotations.Component;

import com.liferay.document.library.kernel.model.DLFolder;
import com.liferay.document.library.kernel.service.DLFolderLocalServiceUtil;
import com.liferay.portal.kernel.exception.PortalException;
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
	long parentFolderId=0;
	long companyId=20116;

	/**
	 * This method is used to trigger one of the methods
	 */
	@Override
	public void init() throws PortletException {
		super.init();
		
		List<DLFolder> folders = getFoldersRecursively(groupId, parentFolderId);
		List<ResourcePermission> resourcePermisions = ResourcePermissionLocalServiceUtil.getResourcePermissions(companyId,DLFolder.class.getName(),ResourceConstants.SCOPE_INDIVIDUAL,String.valueOf(parentFolderId));
		folders.forEach(f->{
				try {
					System.out.println("\n\n\n"+f.getPath());
					printFolderPermissions(f);
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
		List<ResourcePermission> rps = ResourcePermissionLocalServiceUtil.getResourcePermissions(companyId,DLFolder.class.getName(),ResourceConstants.SCOPE_INDIVIDUAL,String.valueOf(f.getFolderId()));
		rps.forEach(rp->{
			List<ResourceAction> resourceActions = ResourceActionLocalServiceUtil.getResourceActions(DLFolder.class.getName());
			try {
				long roleId = rp.getRoleId();
				System.out.print("role="+RoleLocalServiceUtil.getRole(roleId).getName());
			} catch (PortalException e) {
				e.printStackTrace();
			}
			System.out.print(",actionIds="+rp.getActionIds());
			System.out.print(",resourceActionIds="+resourceActions);
			System.out.println("");
		});
	}

	/**
	 * Propagates the permissions of a folder to the other folders
	 * @param f
	 */
	private void setFolderPermissions(DLFolder f,List<ResourceAction> resourceActions) {
		resourceActions.forEach(ra->{
			ra.getActionId();
			//ra.s
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
		List<DLFolder> result= new ArrayList<DLFolder>();
		if(folders.isEmpty()) return folders;
		else
		folders.forEach(f->{
			result.add(f);
			getFoldersRecursively(groupId, f.getFolderId()).forEach(f2->result.add(f2));
		});
		return result;
	}

}