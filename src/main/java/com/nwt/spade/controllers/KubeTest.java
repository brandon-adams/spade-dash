package com.nwt.spade.controllers;

import java.util.List;
import java.util.Map;
import java.util.Set;

import io.fabric8.kubernetes.api.Kubernetes;
import io.fabric8.kubernetes.api.KubernetesFactory;
import io.fabric8.kubernetes.api.KubernetesHelper;
import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.ContainerManifest;
import io.fabric8.kubernetes.api.model.ContainerStatus;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodList;
import io.fabric8.kubernetes.api.model.PodState;

public class KubeTest {

	private void createPodTest() {
		String kubeApi = "http://192.168.4.45:8888";
		try {
			KubernetesFactory kubeFactory = new KubernetesFactory(kubeApi);
			Kubernetes kube = kubeFactory.createKubernetes();
			System.out.println("Connecting to kubernetes on: "
					+ kubeFactory.getAddress());
			System.out.println(kube.getMinions());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	protected static void listPods(Kubernetes kube) {
		System.out.println("Looking up pods");
		PodList pods = kube.getPods();
		// System.out.println("Got pods: " + pods);
		List<Pod> items = pods.getItems();
		for (Pod item : items) {
			System.out.println("Pod " + item.getId() + " created: "
					+ item.getCreationTimestamp());
			PodState desiredState = item.getDesiredState();
			if (desiredState != null) {
				ContainerManifest manifest = desiredState.getManifest();
				if (manifest != null) {
					List<Container> containers = manifest.getContainers();
					if (containers != null) {
						for (Container container : containers) {
							System.out.println("Container "
									+ container.getImage() + " "
									+ container.getCommand() + " ports: "
									+ container.getPorts());
						}
					}
				}
			}
			Map<String, ContainerStatus> currentContainers = KubernetesHelper
					.getCurrentContainers(item);
			System.out.println("Has " + currentContainers.size()
					+ " container(s)");
			Set<Map.Entry<String, ContainerStatus>> entries = currentContainers
					.entrySet();
			for (Map.Entry<String, ContainerStatus> entry : entries) {
				String id = entry.getKey();
				ContainerStatus info = entry.getValue();
				System.out.println("Current container: " + id + " info: "
						+ info);
			}
		}
	}

	public static void main(String[] args) {
		KubeTest test = new KubeTest();
		test.createPodTest();

	}

}
